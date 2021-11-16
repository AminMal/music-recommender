package ir.ac.usc
package controllers

import akka.actor.Actor
import models.{RecommenderFactorizationModel, Song, User}
import utils.ResultsHelper

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import Bootstrap.recommenderActor
import controllers.RecommendationController.Responses.RecommendationResult

import akka.http.scaladsl.server.Route
import akka.pattern.{ask, pipe}
import controllers.RecommendationController.defaultTrendingSongs

class RecommendationController extends Actor {

  import RecommendationController.Messages._
  import RecommendationController.Responses._
  import Bootstrap.system.dispatcher

  val resultsHelper = new ResultsHelper()

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case UpdateContext(model) =>
      context.become(receiveWithModel(model))
      sender() ! ContextUpdated

    case GetRecommendations(userId, _) =>
      sender() ! RecommendationResult(
        userId = userId,
        songs = defaultTrendingSongs,
        actorName = self.path.name
      )
    case GetUserRecommendations(user, _) =>
      sender() ! RecommendationResult(
        user.userId,
        songs = defaultTrendingSongs,
        actorName = self.path.name
      )

    case _ => ModelNotTrainedYet
  }

  def receiveWithModel(model: RecommenderFactorizationModel): Receive = {
    case UpdateContext(newModel) =>
      context.become(receiveWithModel(newModel))
      sender() ! ContextUpdated

    case GetUserRecommendations(user, count) =>
      val recommendationsFuture = model.recommendProductsFuture(user.userId, count)
      val songsFuture = recommendationsFuture.map { recommendations =>
        recommendations.map(rating => resultsHelper.getSongInfo(rating.product))
      }
      val recommendationResult = songsFuture.map { songs =>
        RecommendationResult(
          userId = user.userId,
          songs = songs.filter(_.isDefined).map(_.get),
          actorName = self.path.name
        )
      }
      recommendationResult pipeTo sender()

    case GetRecommendations(userId, count) =>
      val recommendationsFuture = model.recommendProductsFuture(userId, count)
      val songsFuture = recommendationsFuture.map { recommendations =>
        recommendations.map(rating => resultsHelper.getSongInfo(rating.product))
      }
      val recommendationResult = songsFuture.map { songs =>
        RecommendationResult(
          userId = userId,
          songs = songs.filter(_.isDefined).map(_.get),
          actorName = self.path.name
        )
      }

      recommendationResult pipeTo sender()
  }

}

object RecommendationController {
  object Messages {
    case class UpdateContext(model: RecommenderFactorizationModel)
    case class GetUserRecommendations(user: User, count: Int = 6)
    case class GetRecommendations(userId: Int, count: Int = 6)
  }
  object Responses {
    case object ContextUpdated
    case object ModelNotTrainedYet
    case class RecommendationResult(
                                   userId: Long,
                                   actorName: String,
                                   songs: Seq[Song]
                                   )
  }

  import Bootstrap.JsonImplicits._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val defaultTrendingSongs: Seq[Song] = Seq(
    Song(id = 125323, name = "Mu universe", Some("Coldplay")),
    Song(id = 321534, name = "Do I wanna know?", Some("Arctic monkeys")),
    Song(id = 413416, name = "Love and hate", Some("Michael Kiwanuka")),
    Song(id = 782351, name = "Riders on the storm", Some("The doors")),
    Song(id = 213632, name = "Lucky town", Some("Bruce Springsteen")),
    Song(id = 783294, name = "Paragon", Some("Soen"))
  )

  def routes(implicit timeout: Timeout): Route = path("recommend" / Segment) { userId =>
    val result = (recommenderActor ? RecommendationController.Messages.GetRecommendations(userId.toInt)).mapTo[RecommendationResult]
    onSuccess(result) { res =>
      complete(status = StatusCodes.OK, v = res)
    }
  }
}
