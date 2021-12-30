package ir.ac.usc
package service.impl

import akka.actor.ActorRef
import models.RecommendationResult
import service.algebra.RecommendationServiceAlgebra
import controllers.RecommenderManagerActor.Messages._
import controllers.RecommendationController.Messages._

import akka.pattern.ask
import akka.util.Timeout
import utils.box.{BoxF, BoxSupport}

import scala.concurrent.{ExecutionContext, Future}

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
