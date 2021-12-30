package scommender
package service.impl

import controllers.RecommendationController.Messages._
import controllers.RecommenderManagerActor.Messages._
import models.RecommendationResult
import service.algebra.RecommendationServiceAlgebra
import utils.box.{BoxF, BoxSupport}

import akka.actor.ActorRef
import akka.util.Timeout

import scala.concurrent.ExecutionContext

class RecommendationService(recommendationManager: ActorRef)
                           (
                             implicit ec: ExecutionContext,
                             timeout: Timeout
                           ) extends RecommendationServiceAlgebra with BoxSupport {

  override def getRecommendations(userId: Int, count: Int): BoxF[RecommendationResult] = {
    val recommenderBoxF: BoxF[ActorRef] = recommendationManager ??[ActorRef] NewRecommenderActor

    for {
      recommender <- recommenderBoxF
      recommendations <- (recommender ??[RecommendationResult] GetRecommendations(userId, count))
    } yield recommendations

  }

}
