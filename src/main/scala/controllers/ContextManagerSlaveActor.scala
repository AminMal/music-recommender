package ir.ac.usc
package controllers

import conf.ALSDefaultConf
import controllers.ContextManagerActor.Messages._
import controllers.ContextManagerActor.Responses
import models.{SongDTO, User}
import utils.ALSBuilder
import utils.DataFrames.{ratingsRddF, trainRddF}
import conf.{RecommenderDataPaths => Paths}

import java.time.temporal.ChronoUnit.SECONDS
import akka.pattern.pipe
import akka.actor.{Actor, ActorLogging, ActorRef}
import org.apache.spark.sql.SaveMode

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import scala.util.{Failure, Success, Try}


private[controllers] class ContextManagerSlaveActor extends Actor with ActorLogging {
  import ContextManagerActor.Responses._
  import Bootstrap.spark
  import spark.implicits._
  import ContextManagerSlaveActor._

  override def postStop(): Unit =
    log.info(s"(${self.path.name}) got poison pill from parent")

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
      import context.dispatcher
      timeTrack {
        ratingsRddF map (ALSBuilder.forConfig(ALSDefaultConf).run) map(
            SuccessfulUpdateOnModel.apply
          ) pipeTo sender
      } (operationName = Some("updating model"))

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

  private def timeTrack[V](code: => V)(operationName: Option[String] = None, timeUnit: ChronoUnit = SECONDS): V = {
    val start = LocalTime.now()
    val result = code
    val finish = LocalTime.now()
    println(operationName.map(operation => s"Finished $operation, ").getOrElse("") + s"operation took ${timeUnit.between(start, finish)} seconds")
    result
  }
}
