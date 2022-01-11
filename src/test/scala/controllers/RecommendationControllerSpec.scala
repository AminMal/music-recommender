package scommender
package controllers

import akka.actor.{ActorSystem, Props}
import models.RecommendationResult

import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import exception.ModelNotTrainedYetException
import utils.box.Failed

class RecommendationControllerSpec extends TestKit(ActorSystem("recommendation-controller"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  import controllers.RecommendationController.Messages._

  "a recommender actor" should {
    "return failed box when model is not yet trained" in {
      val recommenderActor = system.actorOf(Props[RecommendationController])
      val resultCount = 5
      recommenderActor ! GetRecommendations(userId = 7, resultCount)

      expectMsg(
        Failed[RecommendationResult](ModelNotTrainedYetException)
      )

    }
  }

}
