package ir.ac.usc
package controllers

import akka.actor.{ActorRef, Props}

class RecommenderManagerActorSpec extends ScommenderBaseTestkit {

  import RecommenderManagerActor.Messages._

  "recommender manager actor" should {
    "send back a recommender actor reference in future" in {
      val recommenderManager = system.actorOf(Props[RecommenderManagerActor])
      recommenderManager ! NewRecommenderActor
      expectMsgType[ActorRef]
    }
  }

}
