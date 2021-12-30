package scommender
package controllers

import Bootstrap.services.{performanceEvaluatorService => performanceEvaluator}
import Bootstrap.spark
import conf.{ALSDefaultConf, RecommenderDataPaths}
import models.{SongDTO, User}
import utils.Common.timeTrack
import utils.box.BoxSupport

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration


/**
 * This actor is the main actor of the application, takes the responsibility of (automatically) updating
 * Matrix factorization model, managing slaves in order to create models, add data (users, songs, ratings).
 * There is only one reference of this actor available and created throughout the application.
 */
class ContextManagerActor extends Actor with ActorLogging with BoxSupport {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  import ContextManagerActor._

  log.info("scheduler is starting to work")
  context.system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = ALSDefaultConf.updateInterval
  )(() => self ! UpdateModel)(context.dispatcher)

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    /*
    *    Matrix model messages
    * */
    case UpdateModel =>
      log.info(s"updating model started on context manager actor: ${self.path}")
      val modelBox = toBox {
        timeTrack(operationName = Some("loading latest model"), ChronoUnit.MILLIS) {
          MatrixFactorizationModel
            .load(spark.sparkContext, RecommenderDataPaths.latestModelPath)
        }
      }

      modelBox.peek { model =>
        context.become(receiveWithLatestModel(model))
        performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
      }
        .ifFailed { _ =>
          log.warning("could not find latest model")
          newSlave ! UpdateModel
        }

    case GetLatestModel =>
      sender() ! toBox(Option.empty[MatrixFactorizationModel])

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill
      newSlave ! Save(model)
      performanceEvaluator.evaluateUsingAllMethodsDispatched(model)

    /*
    *   Data append messages
    * */
    case request: AddUserRating =>
      newSlave ! AddDataRequestWithSender(request, sender())

    case request: AddUser =>
      newSlave ! AddDataRequestWithSender(request, sender())

    case request: AddSong =>
      newSlave ! AddDataRequestWithSender(request, sender())

    /*
    *   Slave messages
    * */
    case OperationBindResult(result, replyTo) =>
      replyTo ! toBox(result)
      sender() /* slave */ ! PoisonPill

    case _: CMOperationResult =>
      sender() ! PoisonPill
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

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      newSlave ! Save(model)
      sender() ! PoisonPill

    /*
    *    Data append messages
    * */

    case request: AddUserRating =>
      newSlave ! AddDataRequestWithSender(request, sender())

    case request: AddUser =>
      newSlave ! AddDataRequestWithSender(request, sender())

    case request: AddSong =>
      newSlave ! AddDataRequestWithSender(request, sender())

    /*
    *   Slave messages
    * */

    case OperationBindResult(result, replyTo) =>
      replyTo ! toBox(result)
      sender() /* slave */ ! PoisonPill

    case _: CMOperationResult =>
      sender() ! PoisonPill
  }
}

object ContextManagerActor {

  /**
   * Generates context manager actor Props in order to create new reference of this actor.
   *
   * @return Props of this actor.
   */
  def props: Props = Props(new ContextManagerActor)

  private def newSlave(implicit context: ActorContext): ActorRef = context.actorOf(Props[ContextManagerSlaveActor])

  /**
   * Messages that this actor accepts.
   */
  object Messages {
    sealed trait AddDataRequest

    case class AddDataRequestWithSender(request: AddDataRequest, replyTo: ActorRef)

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
    sealed trait CMOperationResult // ContextManagerOperationResult

    case class SuccessfulUpdateOnModel(model: MatrixFactorizationModel)

    case class OperationFailure(throwable: Throwable) extends CMOperationResult

    case class OperationBindResult(result: CMOperationResult, replyTo: ActorRef)

    case object SuccessfulOperation extends CMOperationResult
  }
}