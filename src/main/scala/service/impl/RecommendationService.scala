package ir.ac.usc
package service.impl

import akka.actor.ActorRef
import models.RecommendationResult
import service.algebra.RecommendationServiceAlgebra
import controllers.RecommenderManagerActor.Messages._
import controllers.RecommendationController.Messages._

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

class RecommendationService(recommendationManager: ActorRef)
                           (
                             implicit ec: ExecutionContext,
                             timeout: Timeout
                           ) extends RecommendationServiceAlgebra {

  override def getRecommendations(userId: Int, count: Int): Future[RecommendationResult] = {
    val futureRecommender = (recommendationManager ? NewRecommenderActor).mapTo[ActorRef]
    for {
      recommender <- futureRecommender
      recommendations <- (recommender ? GetRecommendations(userId, count)).mapTo[RecommendationResult]
    } yield recommendations
  }

}
