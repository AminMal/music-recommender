package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import utils.DataFrames._
import conf.{ALSDefaultConf, RecommenderDataPaths => Paths}
import controllers.ContextManagerActor.ContextManagerSlaveActor
import controllers.ContextManagerActor.Messages.{AddSong, AddUser, AddUserRating, UpdateModel}
import controllers.RecommendationController.Messages.UpdateContext
import utils.ALSBuilder
import models.{SongDTO, User}

import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import HttpServer.contextManagerActor

import akka.http.scaladsl.model.StatusCodes
import models.responses.{ErrorBody, FailureResponse, ResponseMessage, SuccessResponse}

import Bootstrap.system
import org.apache.spark.sql.SaveMode

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}


class ContextManagerActor extends Actor with ActorLogging {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  val system: ActorSystem = context.system

  // retrain model every 2 minutes, todo, should be placed in config
  val updateInterval: FiniteDuration = FiniteDuration(2, TimeUnit.MINUTES)

  log.info("scheduler is starting to work")
  system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = updateInterval
  )(() => self ! UpdateModel)(system.dispatcher)

  def newSlave(): ActorRef = context.actorOf(Props[ContextManagerSlaveActor])

  override def receive: Receive = {

    case UpdateModel =>
      log.info("updating model started on context manager actor")
      newSlave() ! UpdateModel

    case request: AddUserRating =>
      newSlave() ! (request, sender())

    case request: AddUser =>
      newSlave() ! (request, sender())

    case request: AddSong =>
      newSlave() ! (request, sender())

    case OperationBindResult(result, replyTo) =>
      replyTo ! result
      sender() /* slave */ ! PoisonPill

    case SuccessfulOperation =>
      sender() ! PoisonPill
  }
}

object ContextManagerActor {

  sealed class ContextManagerSlaveActor extends Actor with ActorLogging {
    import HttpServer.recommenderActors
    import ContextManagerActor.Responses._
    import Bootstrap.spark
    import spark.implicits._
    import ContextManagerSlaveActor._

    def receive: Receive = {
      case request: (AddUserRating, ActorRef) =>
        val (userRating, replyTo) = request
        handleDFAppend {
          (userRating.decoupled() :: Nil).toDF(AddUserRating.dfColNames: _*)
        } (parent = sender(), replyTo = replyTo)

      case (AddUser(user), replyTo: ActorRef) =>
        /* user validations should be done on another service */
        val newUserDF = (
          user.decoupled() :: Nil
          ).toDF(User.dfColNames: _*)

        handleDFAppend {
          newUserDF.write
            .mode(SaveMode.Append)
            .parquet(path = Paths.usersPath)
        }(parent = sender(), replyTo = replyTo)

      case (AddSong(song), replyTo: ActorRef) =>
        val newSongDF = (
          song.decoupled() :: Nil
          ).toDF(SongDTO.dfColNames: _*)

        handleDFAppend {
          newSongDF.write
            .mode(SaveMode.Append)
            .parquet(path = Paths.songsPath)
        } (parent = sender(), replyTo = replyTo)

      case UpdateModel =>
        val model = ALSBuilder.forConfig(ALSDefaultConf).run(
          ratingsRDD
        )
        log.info(s"model refreshed")
        recommenderActors.foreach(ref => ref ! UpdateContext(model))
        sender() ! SuccessfulOperation

      case unexpectedMessage => system.deadLetters ! unexpectedMessage
    }

  }

  object ContextManagerSlaveActor {
    import Responses._
    def handleDFAppend(code: => Unit)(parent: ActorRef, replyTo: ActorRef): Unit =
      Try(code) match {
        case Failure(exception) =>
          parent ! OperationBindResult(
            OperationFailure(exception),
            replyTo
          )
        case Success(_) =>
          parent ! OperationBindResult(SuccessfulOperation, replyTo)
      }
  }

  object Messages {
    case class AddUserRating(
                            userId: Long,
                            songId: Long,
                            rating: Double
                            ) {
      def decoupled() = (userId, songId, rating)
    }

    object AddUserRating {
      final val dfColNames = Seq("user_id", "song_id", "target")
    }

    case object UpdateModel

    case class AddUser(user: User)

    case class AddSong(song: SongDTO)

  }
  object Responses {
    sealed trait CMOperationResult // ContextManagerOperationResult
    case object SuccessfulOperation extends CMOperationResult
    case class OperationFailure(throwable: Throwable) extends CMOperationResult
    case class OperationBindResult(result: CMOperationResult, replyTo: ActorRef)
  }

  import Bootstrap.JsonImplicits._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import Responses._


  private def completeManagerResult(result: CMOperationResult) = result match {
    case Responses.SuccessfulOperation =>
      complete(
        status = StatusCodes.OK,
        SuccessResponse.forMessage(
          ResponseMessage.ObjectWriteSuccessful
        )
      )
    case OperationFailure(_) =>
      complete(
        status = StatusCodes.InternalServerError,
        FailureResponse.withError(
          error = ErrorBody.InternalServerError
        )
      )
  }

  def routes(implicit timeout: Timeout): Route =
    path("rating") {
      post {
        entity(as[AddUserRating]) { ratingRequest =>
          val managerResponse = (contextManagerActor ? ratingRequest).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeManagerResult)
        }
      }
    } ~ path("user") {
      post {
        entity(as[User]) { user =>
          val managerResponse = (contextManagerActor ? AddUser(user)).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeManagerResult)
        }
      }
    } ~ path("song") {
      post {
        entity(as[SongDTO]) { song =>
          val managerResponse = (contextManagerActor ? AddSong(song)).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeManagerResult)
        }
      }
    }
}