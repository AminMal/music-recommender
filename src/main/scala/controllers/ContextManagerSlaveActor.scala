package scommender
package controllers

import Bootstrap.services.{configurationManagementService => configService}
import conf.{RecommenderDataPaths => Paths}
import controllers.ContextManagerActor.Messages._
import models.{SongDTO, User}
import utils.{ALSBuilder, DataFrameProvider}
import utils.box._

import akka.Done
import akka.actor.{Actor, ActorLogging, PoisonPill}
import org.apache.spark.sql.SaveMode

import java.io.File
import java.time.temporal.ChronoUnit
import scala.util.{Failure, Success, Try}
import utils.TimeUtils.timeTrack
import Bootstrap.spark


/**
 * This actor is used to do time taking, and blocking tasks that context manager actor
 * wants to do. for each command, a new reference is created as a child of context manager actor
 * and gets killed right after the work is done.
 */
private[controllers] class ContextManagerSlaveActor(
                                                   dataFrameProvider: DataFrameProvider
                                                   ) extends Actor with ActorLogging with BoxSupport {

  import ContextManagerActor.Responses._
  import ContextManagerSlaveActor._
  import spark.implicits._


  def receive: Receive = {
    case request: AddUserRating =>
      val writeResult: Box[Done] = controlDFAppend {
        (request :: Nil).toDF(AddUserRating.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.ratingsPath)
      }

      sender() ! writeResult
      self ! PoisonPill

    case request: AddUser =>
      val writeResult: Box[Done] = controlDFAppend {
        (request.user :: Nil).toDF(User.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.usersPath)
      }

      sender() ! writeResult
      self ! PoisonPill

    case request: AddSong =>
      val writeResult: Box[Done] = controlDFAppend {
        (request.song :: Nil).toDF(SongDTO.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.songsPath)
      }

      sender() ! writeResult
      self ! PoisonPill

    case UpdateModel =>
      import context.dispatcher

      val modelBoxF = for {
        config <- toBoxF(configService.getLatestConfig)
        ratings <- dataFrameProvider.trainRddBoxF
      } yield {
        log.info(s"updating model for config: $config")
        timeTrack(operationName = "creating als model", ChronoUnit.MILLIS) {
          ALSBuilder.forConfig(config).run(ratings)
        }
      }

      modelBoxF.map(SuccessfulUpdateOnModel.apply)
        .pipeTo(sender)

    case Save(model) =>
      timeTrack(operationName = "saving latest recommender", ChronoUnit.MILLIS) {
        Box {
          new File(Paths.latestModelPath).delete()
          log.info("deleted latest model")
          model.save(spark.sparkContext, Paths.latestModelPath)
          log.info("inserted latest model")
        }
      }

  }

}

object ContextManagerSlaveActor {

  private def controlDFAppend(code: => Unit): Box[Done] =
    Try(code) match {
      case Failure(exception) =>
        Failed[Done](exception)
      case Success(_) =>
        Successful[Done](Done)
    }
}
