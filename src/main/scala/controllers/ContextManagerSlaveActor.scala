package ir.ac.usc
package controllers

import conf.{RecommenderDataPaths => Paths}
import controllers.ContextManagerActor.Messages._
import controllers.ContextManagerActor.Responses._
import models.{SongDTO, User}
import utils.ALSBuilder
import utils.DataFrames.ratingsRddF

import akka.pattern.pipe
import akka.actor.{Actor, ActorLogging, ActorRef}
import Bootstrap.services.{configurationManagementService => configService}
import org.apache.spark.sql.SaveMode
import scala.util.{Failure, Success, Try}


private[controllers] class ContextManagerSlaveActor extends Actor with ActorLogging {
  import ContextManagerActor.Responses._
  import Bootstrap.spark
  import spark.implicits._
  import ContextManagerSlaveActor._
  import utils.Common.timeTrack

  override def postStop(): Unit =
    log.info(s"(${self.path.name}) got poison pill from parent")

  def receive: Receive = {
    case request: (AddUserRating, ActorRef) =>
      val (userRating, replyTo) = request
      handleDFAppend {
        (userRating.decoupled() :: Nil).toDF(AddUserRating.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.ratingsPath)
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
      import context.dispatcher

      val configFuture = configService.getLatestConfig
      val modelFuture = for {
        config <- configFuture
        ratings <- ratingsRddF
      } yield {
        timeTrack {
          ALSBuilder.forConfig(config).run(ratings)
        } (operationName = Some("creating als model"))
      }

      modelFuture.map(SuccessfulUpdateOnModel.apply)
        .pipeTo(sender)

  }

}

object ContextManagerSlaveActor {
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
