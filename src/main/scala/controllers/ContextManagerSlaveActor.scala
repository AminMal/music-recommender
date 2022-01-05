package scommender
package controllers

import Bootstrap.services.{configurationManagementService => configService}
import conf.{RecommenderDataPaths => Paths}
import controllers.ContextManagerActor.Messages._
import controllers.ContextManagerActor.Responses._
import models.{SongDTO, User}
import utils.{ALSBuilder, DataFrameProvider}
import utils.box.BoxSupport

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.apache.spark.sql.SaveMode

import java.io.File
import java.time.temporal.ChronoUnit
import scala.util.{Failure, Success, Try}


/**
 * This actor is used to do time taking, and blocking tasks that context manager actor
 * wants to do. for each command, a new reference is created as a child of context manager actor
 * and gets killed right after the work is done.
 */
private[controllers] class ContextManagerSlaveActor(
                                                   dataFrameProvider: DataFrameProvider
                                                   ) extends Actor with ActorLogging with BoxSupport {

  import Bootstrap.spark
  import utils.TimeUtils.timeTrack
  import ContextManagerActor.Responses._
  import ContextManagerSlaveActor._
  import spark.implicits._


  override def postStop(): Unit =
    log.info(s"(${self.path.name}) got poison pill from parent")

  def receive: Receive = {
    case AddDataRequestWithSender(request, replyTo) =>
      request match {
        case req: AddUserRating =>
          handleDFAppend {
            (req :: Nil).toDF(AddUserRating.dfColNames: _*)
              .write
              .mode(SaveMode.Append)
              .parquet(path = Paths.ratingsPath)
          }(parent = sender(), replyTo = replyTo)
        case req: AddUser =>
          /* user validations should be done on another service */
          val newUserDF = (req.user :: Nil)
            .toDF(User.dfColNames: _*)

          handleDFAppend {
            newUserDF.write
              .mode(SaveMode.Append)
              .parquet(path = Paths.usersPath)
          }(parent = sender(), replyTo = replyTo)
        case req: AddSong =>
          val newSongDF = (req.song :: Nil)
            .toDF(SongDTO.dfColNames: _*)

          handleDFAppend {
            newSongDF.write
              .mode(SaveMode.Append)
              .parquet(path = Paths.songsPath)
          }(parent = sender(), replyTo = replyTo)
      }

    case UpdateModel =>
      import context.dispatcher

      val modelBoxF = for {
        config <- toBoxF(configService.getLatestConfig)
        ratings <- dataFrameProvider.trainRddBoxF
      } yield {
        log.info(s"updating model for config: $config")
        timeTrack(operationName = Some("creating als model"), ChronoUnit.MILLIS) {
          ALSBuilder.forConfig(config).run(ratings)
        }
      }

      modelBoxF.map(SuccessfulUpdateOnModel.apply)
        .pipeTo(sender)

    case Save(model) =>
      timeTrack(operationName = Some("saving latest recommender"), ChronoUnit.MILLIS) {
        Try {
          new File(Paths.latestModelPath).delete()
          log.info("deleted latest model")
          model.save(spark.sparkContext, Paths.latestModelPath)
          log.info("inserted latest model")
        }
      }

      sender() ! SuccessfulOperation


  }

}

object ContextManagerSlaveActor {
  private def handleDFAppend(code: => Unit)(parent: ActorRef, replyTo: ActorRef): Unit =
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
