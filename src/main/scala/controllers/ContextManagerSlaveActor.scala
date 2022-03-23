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
import utils.TimeUtils.{timeTrack, timeTrackReturningDuration}
import Bootstrap.spark

import service.DiagnosticsService


/**
 * This actor is used to do time taking, and blocking tasks that context manager actor
 * wants to do. for each command, a new reference is created as a child of context manager actor
 * and gets killed right after the work is done.
 */
private[controllers] class ContextManagerSlaveActor(
                                                   dataFrameProvider: DataFrameProvider
                                                   ) extends Actor with ActorLogging with BoxSupport {

  import ContextManagerActor.Responses._
  import spark.implicits._


  def receive: Receive = {
    case request: AddUserRating =>
      val writeResult = toBox {
        (request :: Nil).toDF(AddUserRating.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.ratingsPath)
      } map (_ => Done)

      sender() ! writeResult
      self ! PoisonPill

    case request: AddUser =>
      val writeResult = toBox {
        (request.user :: Nil).toDF(User.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.usersPath)
      } map (_ => Done)

      sender() ! writeResult
      self ! PoisonPill

    case request: AddSong =>
      val writeResult = toBox {
        (request.song :: Nil).toDF(SongDTO.dfColNames: _*)
          .write
          .mode(SaveMode.Append)
          .parquet(path = Paths.songsPath)
      } map (_ => Done)

      sender() ! writeResult
      self ! PoisonPill

    case UpdateModel =>
      import context.dispatcher

      val modelBoxF = for {
        config <- toBoxF(configService.getLatestConfig)
        ratings <- dataFrameProvider.trainRddBoxF
      } yield {
        log.info(s"updating model for config: $config")
        val (model, duration) = timeTrackReturningDuration(operationName = "creating als model", ChronoUnit.MILLIS) {
          ALSBuilder.forConfig(config).run(ratings)
        }
        DiagnosticsService.updateModelTrainingReport(model, duration, config)
        model
      }

      modelBoxF.map(SuccessfulUpdateOnModel.apply) pipeTo sender

    case Save(model) =>
      timeTrack(operationName = "saving latest recommender", ChronoUnit.MILLIS) {
        Box {
          new File(Paths.latestModelPath).delete()
          model.save(spark.sparkContext, Paths.latestModelPath)
        }
      }

  }

}