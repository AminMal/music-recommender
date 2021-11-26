package ir.ac.usc
package controllers

import akka.actor.Props
import controllers.RecommendationController.defaultTrendingSongs

class RecommendationControllerSpec extends ScommenderBaseTestkit {
  import controllers.RecommendationController.Messages._
  import controllers.RecommendationController.Responses._

  "a recommender actor" should {
    "return default recommendations when not trained with model" in {
      val recommenderActor = system.actorOf(Props[RecommendationController])
      val resultCount = 5
      recommenderActor ! GetRecommendations(userId = 7, resultCount)

      expectMsg(
        RecommendationResult(
          userId = 7,
          songs = defaultTrendingSongs.take(resultCount),
          actorName = self.path.name
        )
      )

    }
  }

}
