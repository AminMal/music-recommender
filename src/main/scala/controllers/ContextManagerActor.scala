package scommender
package controllers

import Bootstrap.services.{performanceEvaluatorService => performanceEvaluator}
import Bootstrap.{appConfig, spark}
import conf.RecommenderDataPaths
import models.{SongDTO, User}
import utils.TimeUtils.timeTrackReturningDuration
import utils.box.{Box, BoxSupport, Failed}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import utils.DataFrameProvider

import org.apache.hadoop.mapred.InvalidInputException
import exception.ModelNotTrainedYetException

import service.DiagnosticsService

import java.io.File
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}


/**
 * This actor is the main actor of the application, takes the responsibility of (automatically) updating
 * Matrix factorization model, managing slaves in order to create models, add data (users, songs, ratings).
 * There is only one reference of this actor available and created throughout the application.
 */
class ContextManagerActor(
                         dataframeProducer: () => DataFrameProvider
                         ) extends Actor with ActorLogging with BoxSupport {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses.SuccessfulUpdateOnModel._
  import ContextManagerActor._

  context.system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = ContextManagerActor.overridingInterval
  )(() => self ! UpdateModel)(context.dispatcher)


  private def newSlave(): ActorRef = context.actorOf(Props(
    new ContextManagerSlaveActor(dataframeProducer.apply())
  ))

  private def updateModel(): Unit = newSlave ! UpdateModel

  private def dataAppendReceive(): Receive = {
    case request: AddUserRating =>
      newSlave forward request

    case request: AddUser =>
      newSlave forward request

    case request: AddSong =>
      newSlave forward request
  }

  private def getReceive(modelOpt: Option[MatrixFactorizationModel]): Receive =
    dataAppendReceive() orElse modelOpt.map(contextReceiveWithModel).getOrElse(contextReceiveWithoutModel)

  override def receive: Receive = getReceive(modelOpt = None)

  def contextReceiveWithoutModel: Receive = {
    case UpdateModel =>
      log.info(s"updating model started on context manager actor")
      if (cacheMatrixModel) {
        val cachedModel = if (new File(RecommenderDataPaths.latestModelPath).exists()) toBox {
          val (model, duration) = timeTrackReturningDuration(operationName = "loading latest model", ChronoUnit.MILLIS) {
            MatrixFactorizationModel
              .load(spark.sparkContext, RecommenderDataPaths.latestModelPath)
          }
          DiagnosticsService.updateModelLoadingTimeReport(model, duration)
          model
        } else {
          Failed[MatrixFactorizationModel](ModelNotTrainedYetException)
        }

        cachedModel peek { model =>
          context become getReceive(Some(model))
          if (performanceTestOnReload) {
            performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
          }
        } ifFailed {
          case se if se.is[InvalidInputException] => // is an hadoop IOException, since no latest model was persisted before
            updateModel()
        }

      } else updateModel()

    case GetLatestModel =>
      sender() ! toBox(Option.empty[MatrixFactorizationModel])

    case UpdateSuccessful(model) =>
      context become getReceive(Some(model))
      sender() ! PoisonPill
      if (cacheMatrixModel) {
        newSlave ! Save(model)
      }
      if (performanceTestOnReload)
        performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
  }

  def contextReceiveWithModel(model: MatrixFactorizationModel): Receive = {
    case UpdateModel =>
      log.info(s"updating model started on context manager actor")
      updateModel()

    case GetLatestModel =>
      sender() ! toBox(Option.apply[MatrixFactorizationModel](model))

    case UpdateSuccessful(model) =>
      context become getReceive(Some(model))
      sender() ! PoisonPill
      if (cacheMatrixModel) {
        newSlave ! Save(model)
      }
      if (performanceTestOnReload) {
        performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
      }
  }
}

object ContextManagerActor {

  private final val performanceTestOnReload = appConfig.getBoolean("scommender.performance-test-on-reload")
  private final val cacheMatrixModel = appConfig.getBoolean("scommender.cache-matrix")
  private def overridingInterval: FiniteDuration = FiniteDuration(
    length = appConfig.getInt("scommender.update-interval.value"),
    unit = TimeUnit.valueOf(appConfig.getString("scommender.update-interval.time-unit"))
  )

  /**
   * Generates context manager actor Props in order to create new reference of this actor.
   *
   * @return Props of this actor.
   */
  def props(dataframeProducer: () => DataFrameProvider): Props = Props(new ContextManagerActor(dataframeProducer))

  /**
   * Messages that this actor accepts.
   */
  object Messages {
    sealed trait AddDataRequest

    case class AddUserRating(
                              userId: Long,
                              songId: Long,
                              rating: Double
                            ) extends AddDataRequest

    case class AddUser(user: User) extends AddDataRequest

    case class AddSong(song: SongDTO) extends AddDataRequest

    case class Save(model: MatrixFactorizationModel)

    object AddUserRating {
      final val dfColNames = Seq("user_id", "song_id", "target")
    }

    case object UpdateModel

    case object GetLatestModel
  }

  /**
   * Responses that this actor generates.
   */
  object Responses {
    case class SuccessfulUpdateOnModel(model: MatrixFactorizationModel)

    object SuccessfulUpdateOnModel {
      object UpdateSuccessful {
        def unapply(value: Box[SuccessfulUpdateOnModel]): Option[MatrixFactorizationModel] =
          value.toOption.map(_.model)
      }
    }
  }
}