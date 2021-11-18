package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorSystem}
import conf.ALSDefaultConf
import controllers.RecommendationController.Messages.UpdateContext
import utils.ALSBuilder
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.SaveMode
import akka.pattern.pipe

import scala.concurrent.Future

class ContextManagerActor extends Actor {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  import Bootstrap.DataFrames._
  import Bootstrap.spark.implicits._
  val system: ActorSystem = context.system
  import system.dispatcher
  import HttpServer.recommenderActors

  override def receive: Receive = ???

  def newTimeBasedReceive(accumulatedRatings: Seq[Rating]): Receive = {
    case Initialize =>
      if (sender() != self)
        sender() ! Forbidden
      else {
        val updateMessage = Future {
          val newRatingsDF = accumulatedRatings.toDF
          ratingsDF.union(newRatingsDF)
            .write
            .mode(SaveMode.Append)
            .save()

          val newModel = ALSBuilder.forConfig(ALSDefaultConf)
            .run(
              newRatingsDF.rdd.map { row =>
                Rating(
                  user = row.getString(0).toInt,
                  product = row.getString(1).toInt,
                  rating = row.getString(2).toDouble
                )
              }
            )
          // on model update:
          UpdateContext(newModel)
        }
        recommenderActors.foreach(actor => updateMessage pipeTo actor)
        sender() ! ContextsUpdated
      }

  }
}

object ContextManagerActor {
  object Messages {
    case class AddUserRating(
                            userId: Int,
                            songId: Int,
                            rating: Double
                            )

    case object Initialize

  }
  object Responses {
    sealed trait ContextManagerResponse
    case object Forbidden extends ContextManagerResponse
    case object ContextsUpdated
  }
}