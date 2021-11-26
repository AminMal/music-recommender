package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import HttpServer.contextManagerActor

import akka.util.Timeout
import controllers.RecommendationController.Messages.UpdateContext

import java.util.concurrent.TimeUnit
import scala.concurrent.Future


class RecommenderManagerActor extends Actor {
  import controllers.RecommenderManagerActor.Messages._
  import controllers.ContextManagerActor.Messages._
  import context.dispatcher
  val contextManagerMessageTimeout: Timeout = Timeout(5, TimeUnit.SECONDS)

  def newRecommender(): ActorRef = context.actorOf(Props[RecommendationController])

  def receive: Receive = {
    case NewRecommenderActor =>
      val latestModelOptional: Future[Option[MatrixFactorizationModel]] =
        (contextManagerActor ? GetLatestModel)(contextManagerMessageTimeout).mapTo[Option[MatrixFactorizationModel]]

      val futureActor = latestModelOptional.map { modelOpt =>
        val recommender = newRecommender()
        modelOpt.foreach(model => recommender ! UpdateContext(model))
        recommender
      }

      futureActor pipeTo sender()
  }

}

object RecommenderManagerActor {

  object Messages {
    case object NewRecommenderActor
  }

}