package scommender
package controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class RecommenderManagerActorSpec extends TestKit(ActorSystem("recommendation-manager"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import RecommenderManagerActor.Messages._

  "recommender manager actor" should {
    "send back a recommender actor reference in future" in {
      val recommenderManager = system.actorOf(Props[RecommenderManagerActor])
      recommenderManager ! NewRecommenderActor
      expectMsgType[ActorRef]
    }
  }

}
