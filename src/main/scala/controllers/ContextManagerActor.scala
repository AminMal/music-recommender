package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import utils.DataFrames._
import conf.{ALSDefaultConf, RecommenderDataPaths => Paths}
import controllers.ContextManagerActor.ContextManagerSlaveActor
import controllers.ContextManagerActor.Messages.UpdateModel
import controllers.RecommendationController.Messages.UpdateContext
import utils.ALSBuilder

import org.apache.spark.sql.{DataFrame, SaveMode}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}


class ContextManagerActor extends Actor with ActorLogging {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  val system: ActorSystem = context.system
  import Bootstrap.spark
  import spark.implicits._
  import ContextManagerActor.ContextManagerSlaveMessages._

  // retrain model every 2 minutes, todo, should be placed in config
  val updateInterval: FiniteDuration = FiniteDuration(2, TimeUnit.MINUTES)

  println("scheduler is starting to work")
  system.scheduler.scheduleAtFixedRate(
    initialDelay = Duration.Zero, interval = updateInterval
  )(() => self ! UpdateModel)(system.dispatcher)

  override def receive: Receive = {
    case AddUserRating(userId, songId, rate) =>
      val rating = Seq((userId, songId, rate)).toDF
      val slave = context.actorOf(Props[ContextManagerSlaveActor])
      slave ! AppendRating(rating, sender())


    case UpdateModel =>
      log.info("updating model started on context manager actor")
      val slave = context.actorOf(Props[ContextManagerSlaveActor])
      slave ! UpdateModel


    case OperationBindResult(result, replyTo) =>
      replyTo ! result
      sender() /* slave */ ! PoisonPill
  }
}

object ContextManagerActor {

  sealed class ContextManagerSlaveActor extends Actor with ActorLogging {
    import ContextManagerSlaveMessages._
    import HttpServer.recommenderActors
    import ContextManagerActor.Responses._

    def receive: Receive = {
      case AppendRating(rates, replyTo) =>
        Try {
          rates.write
            .mode(SaveMode.Append)
            .parquet(path = Paths.ratingsPath)
        } match {
          case Failure(throwable) =>
            sender() ! OperationBindResult(OperationFailure(throwable), replyTo)
          case Success(_) =>
            sender() ! OperationBindResult(SuccessfulOperation, replyTo)
        }


      case UpdateModel =>
        val model = ALSBuilder.forConfig(ALSDefaultConf).run(
          ratingsRDD
        )
        log.info(s"model refreshed")
        recommenderActors.foreach(ref => ref ! UpdateContext(model))
        sender() ! SuccessfulOperation
    }

  }

  object ContextManagerSlaveMessages {
    case class AppendRating(rates: DataFrame, replyTo: ActorRef)
  }

  object Messages {
    case class AddUserRating(
                            userId: Long,
                            songId: Long,
                            rating: Double
                            )

    case object UpdateModel

  }
  object Responses {
    sealed trait CMOperationResult // ContextManagerOperationResult
    case object SuccessfulOperation extends CMOperationResult
    case class OperationFailure(throwable: Throwable) extends CMOperationResult
    case class OperationBindResult(result: CMOperationResult, replyTo: ActorRef)
  }
}