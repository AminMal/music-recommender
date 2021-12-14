package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.pipe
import controllers.RecommendationController.Messages.UpdateContext


class RecommenderManagerActor extends Actor {
  import controllers.RecommenderManagerActor.Messages._
  import context.dispatcher
  import Bootstrap.services.contextManagerService

  def newRecommender(): ActorRef = context.actorOf(RecommendationController.props)

  def receive: Receive = {
    case NewRecommenderActor =>
      val latestModelOptional= contextManagerService.getLatestModel

      val futureActor = latestModelOptional.map { modelOpt =>
        val recommender = newRecommender()
        modelOpt.foreach(model => recommender ! UpdateContext(model))
        recommender
      }

      futureActor pipeTo sender()
  }

}

object RecommenderManagerActor {

  def props: Props = Props(new RecommenderManagerActor)

  object Messages {
    case object NewRecommenderActor
  }

}