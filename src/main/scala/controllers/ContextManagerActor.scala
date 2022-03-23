package scommender
package controllers

import Bootstrap.services.{performanceEvaluatorService => performanceEvaluator}
import Bootstrap.{appConfig, spark}
import conf.{ALSDefaultConf, RecommenderDataPaths}
import models.{SongDTO, User}
import utils.TimeUtils.timeTrack
import utils.box.{Box, BoxSupport}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import utils.DataFrameProvider

import org.apache.hadoop.mapred.InvalidInputException

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration


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

  context.system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = ALSDefaultConf.updateInterval
  )(() => self ! UpdateModel)(context.dispatcher)


  private def newSlave(): ActorRef = context.actorOf(Props(
    new ContextManagerSlaveActor(dataframeProducer.apply())
  ))


  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    /*
    *    Matrix model messages
    * */
    case UpdateModel =>
      log.info(s"updating model started on context manager actor: ${self.path}")
      if (appConfig.getBoolean("scommender.cache-matrix")) {
        val cachedModelBox = toBox {
          timeTrack(operationName = "loading latest model", ChronoUnit.MILLIS) {
            MatrixFactorizationModel
              .load(spark.sparkContext, RecommenderDataPaths.latestModelPath)
          }
        }
        cachedModelBox peek { model =>
          context.become(receiveWithLatestModel(model))
          if (appConfig.getBoolean("scommender.performance-test-on-startup")) {
            performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
          }
        } ifFailed {
          case se if se.isOfType[InvalidInputException] => // is an IOException, since no latest model was persisted before
            newSlave ! UpdateModel
          case other =>
            log.error(other, "could not load latest model")
        }
      } else newSlave ! UpdateModel

    case GetLatestModel =>
      sender() ! toBox(Option.empty[MatrixFactorizationModel])

    case UpdateSuccessful(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill
      if (appConfig.getBoolean("scommender.cache-matrix")) {
        newSlave ! Save(model)
      }
      performanceEvaluator.evaluateUsingAllMethodsDispatched(model)

    /*
    *   Data append messages
    * */
    case request: AddUserRating =>
      newSlave forward request

    case request: AddUser =>
      newSlave forward request

    case request: AddSong =>
      newSlave forward request
  }

  def receiveWithLatestModel(model: MatrixFactorizationModel): Receive = {
    /*
    *    Matrix model messages
    * */
    case UpdateModel =>
      log.info(s"updating model started on context manager actor: ${self.path}")
      newSlave ! UpdateModel

    case GetLatestModel =>
      sender() ! toBox(Option.apply[MatrixFactorizationModel](model))

    case UpdateSuccessful(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill
      if (appConfig.getBoolean("scommender.cache-matrix")) {
        newSlave ! Save(model)
      }

    /*
    *    Data append messages
    * */

    case request: AddUserRating =>
      newSlave forward request

    case request: AddUser =>
      newSlave forward request

    case request: AddSong =>
      newSlave forward request
  }
}

object ContextManagerActor {

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