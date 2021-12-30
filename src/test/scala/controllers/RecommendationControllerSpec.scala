package scommender
package controllers

import akka.actor.{ActorSystem, Props}
import controllers.RecommendationController.defaultTrendingSongs
import models.RecommendationResult
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class RecommendationControllerSpec extends TestKit(ActorSystem("recommendation-controller"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  import controllers.RecommendationController.Messages._

  "a recommender actor" should {
    "return default recommendations when not trained with model" in {
      val recommenderActor = system.actorOf(Props[RecommendationController])
      val resultCount = 5
      recommenderActor ! GetRecommendations(userId = 7, resultCount)

      expectMsg(
        new RecommendationResult(
          userId = 7,
          songs = defaultTrendingSongs.take(resultCount)
        )
      )

    }
  }

}
