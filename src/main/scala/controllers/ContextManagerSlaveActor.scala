package ir.ac.usc
package controllers

import Bootstrap.system
import conf.ALSDefaultConf
import controllers.ContextManagerActor.Messages.{AddSong, AddUser, AddUserRating, UpdateModel}
import controllers.ContextManagerActor.Responses
import models.{SongDTO, User}
import utils.ALSBuilder
import utils.DataFrames.ratingsRDD
import conf.{RecommenderDataPaths => Paths}
import akka.actor.{Actor, ActorLogging, ActorRef}
import org.apache.spark.sql.SaveMode

import scala.util.{Failure, Success, Try}


private[controllers] class ContextManagerSlaveActor extends Actor with ActorLogging {
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
      sender() ! SuccessfulUpdateOnModel(model)

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
