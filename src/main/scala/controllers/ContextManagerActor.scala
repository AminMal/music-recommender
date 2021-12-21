package ir.ac.usc
package controllers

import Bootstrap.services.{performanceEvaluatorService => performanceEvaluator}
import Bootstrap.spark
import conf.{ALSDefaultConf, RecommenderDataPaths}
import models.{SongDTO, User}
import utils.Common.timeTrack

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration


class ContextManagerActor extends Actor with ActorLogging {

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
      try {
        val model = timeTrack {
          MatrixFactorizationModel
            .load(spark.sparkContext, RecommenderDataPaths.latestModelPath)
        } (operationName = Some("loading latest model"), ChronoUnit.MILLIS)

        context.become(receiveWithLatestModel(model))
        performanceEvaluator.evaluateUsingAllMethodsDispatched(model)
      } catch {
        case _: Exception =>
          log.warning("could not find latest model")
          newSlave ! UpdateModel
      }

    case GetLatestModel =>
      sender() ! Option.empty[MatrixFactorizationModel]

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      sender() ! PoisonPill
      newSlave ! Save(model)
      performanceEvaluator.evaluateUsingAllMethodsDispatched(model)

      /*
      *   Data append messages
      * */
    case request: AddUserRating =>
      newSlave ! (request, sender())

    case request: AddUser =>
      newSlave ! (request, sender())

    case request: AddSong =>
      newSlave ! (request, sender())

      /*
      *   Slave messages
      * */
    case OperationBindResult(result, replyTo) =>
      replyTo ! result
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
      sender() ! Option.apply[MatrixFactorizationModel](model)

    case SuccessfulUpdateOnModel(model) =>
      context.become(receiveWithLatestModel(model))
      newSlave ! Save(model)
      sender() ! PoisonPill

      /*
      *    Data append messages
      * */

    case request: AddUserRating =>
      newSlave ! (request, sender())

    case request: AddUser =>
      newSlave ! (request, sender())

    case request: AddSong =>
      newSlave ! (request, sender())

      /*
      *   Slave messages
      * */

    case OperationBindResult(result, replyTo) =>
      replyTo ! result
      sender() /* slave */ ! PoisonPill

    case _: CMOperationResult =>
      sender() ! PoisonPill
  }
}

object ContextManagerActor {

  def props: Props = Props(new ContextManagerActor)

  private def newSlave(implicit context: ActorContext): ActorRef = context.actorOf(Props[ContextManagerSlaveActor])

  object Messages {
    case class AddUserRating(
                            userId: Long,
                            songId: Long,
                            rating: Double
                            )

    object AddUserRating {
      final val dfColNames = Seq("user_id", "song_id", "target")
    }

    case object UpdateModel

    case class AddUser(user: User)

    case class AddSong(song: SongDTO)

    case object GetLatestModel

    case class Save(model: MatrixFactorizationModel)
  }
  object Responses {
    sealed trait CMOperationResult // ContextManagerOperationResult
    case class SuccessfulUpdateOnModel(model: MatrixFactorizationModel)
    case object SuccessfulOperation extends CMOperationResult
    case class OperationFailure(throwable: Throwable) extends CMOperationResult
    case class OperationBindResult(result: CMOperationResult, replyTo: ActorRef)
  }
}