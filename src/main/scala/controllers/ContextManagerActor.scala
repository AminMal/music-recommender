package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import models.{SongDTO, User}

import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import HttpServer.contextManagerActor

import akka.http.scaladsl.model.StatusCodes
import models.responses.{ErrorBody, FailureResponse, ResponseMessage, SuccessResponse}

import conf.ALSDefaultConf
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.duration.Duration


class ContextManagerActor extends Actor with ActorLogging {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  val system: ActorSystem = context.system

  log.info("scheduler is starting to work")
  system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = ALSDefaultConf.updateInterval
  )(() => self ! UpdateModel)(system.dispatcher)

  def newSlave(): ActorRef = context.actorOf(Props[ContextManagerSlaveActor])

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    /*
    *    Matrix model messages
    * */
    case UpdateModel =>
      log.info("updating model started on context manager actor")
      newSlave() ! UpdateModel

    case GetLatestModel =>
      sender() ! Option.empty[MatrixFactorizationModel]

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill

      /*
      *   Data append messages
      * */
    case request: AddUserRating =>
      newSlave() ! (request, sender())

    case request: AddUser =>
      newSlave() ! (request, sender())

    case request: AddSong =>
      newSlave() ! (request, sender())

      /*
      *   Slave messages
      * */
    case OperationBindResult(result, replyTo) =>
      replyTo ! result
      sender() /* slave */ ! PoisonPill
  }

  def receiveWithLatestModel(model: MatrixFactorizationModel): Receive = {
    /*
    *    Matrix model messages
    * */
    case UpdateModel =>
      log.info("updating model started on context manager actor")
      newSlave() ! UpdateModel

    case GetLatestModel =>
      sender() ! Option.apply[MatrixFactorizationModel](model)

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill

      /*
      *    Data append messages
      * */

    case request: AddUserRating =>
      newSlave() ! (request, sender())

    case request: AddUser =>
      newSlave() ! (request, sender())

    case request: AddSong =>
      newSlave() ! (request, sender())

      /*
      *   Slave messages
      * */

    case OperationBindResult(result, replyTo) =>
      replyTo ! result
      sender() /* slave */ ! PoisonPill
  }
}

object ContextManagerActor {

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

    case object GetLatestModel

  }
  object Responses {
    sealed trait CMOperationResult // ContextManagerOperationResult
    case class SuccessfulUpdateOnModel(model: MatrixFactorizationModel)
    case object SuccessfulOperation extends CMOperationResult
    case class OperationFailure(throwable: Throwable) extends CMOperationResult
    case class OperationBindResult(result: CMOperationResult, replyTo: ActorRef)
  }

  import Bootstrap.JsonImplicits._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import Responses._


  private def completeWriteResult(result: CMOperationResult) = result match {
    case SuccessfulOperation =>
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

  import Messages._

  def routes(implicit timeout: Timeout): Route =
    path("rating") {
      post {
        entity(as[AddUserRating]) { ratingRequest =>
          val managerResponse = (contextManagerActor ? ratingRequest).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeWriteResult)
        }
      }
    } ~ path("user") {
      post {
        entity(as[User]) { user =>
          val managerResponse = (contextManagerActor ? AddUser(user)).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeWriteResult)
        }
      }
    } ~ path("song") {
      post {
        entity(as[SongDTO]) { song =>
          val managerResponse = (contextManagerActor ? AddSong(song)).mapTo[CMOperationResult]

          onSuccess(managerResponse) (completeWriteResult)
        }
      }
    } ~ path("force-update") {
      put {
        contextManagerActor ! UpdateModel
        complete(
          status = StatusCodes.OK,
          SuccessResponse.forMessage("update request sent successfully")
        )
      }
    }
}