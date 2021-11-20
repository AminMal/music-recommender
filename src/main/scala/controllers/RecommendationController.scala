package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging}
import models.{Song, User}
import utils.ResultsHelper

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import HttpServer.recommenderActor
import controllers.RecommendationController.Responses.RecommendationResult

import akka.http.scaladsl.server.Route
import akka.pattern.ask
import controllers.RecommendationController.defaultTrendingSongs

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

class RecommendationController extends Actor with ActorLogging {

  import RecommendationController.Messages._
  import RecommendationController.Responses._

  val resultsHelper = new ResultsHelper()

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case UpdateContext(model) =>
      context.become(receiveWithModel(model))
      log.info(s"update factorization model in recommender actor")
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

  def receiveWithModel(model: MatrixFactorizationModel): Receive = {
    case UpdateContext(newModel) =>
      context.become(receiveWithModel(newModel))
      sender() ! ContextUpdated

    case GetUserRecommendations(user, count) =>
      val recommendations = model.recommendProducts(user.userId, count)
      val songs = recommendations.map(rating => resultsHelper.getSongInfo(rating.product))
      val recommendationResult =
        RecommendationResult(
          userId = user.userId,
          songs = songs.filter(_.isDefined).map(_.get),
          actorName = self.path.name
        )

      sender() ! recommendationResult

    case GetRecommendations(userId, count) =>
      val recommendations = model.recommendProducts(userId, count)
      val songs = recommendations.map(rating => resultsHelper.getSongInfo(rating.product))

      val recommendationResult =
        RecommendationResult(
          userId = userId,
          songs = songs.filter(_.isDefined).map(_.get),
          actorName = self.path.name
        )


      sender() ! recommendationResult
  }

}

object RecommendationController {
  object Messages {
    case class UpdateContext(model: MatrixFactorizationModel)
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
    val result = (recommenderActor ? RecommendationController.Messages.GetRecommendations(userId.toInt))
      .mapTo[RecommendationResult]

    onSuccess(result) { res =>
      complete(status = StatusCodes.OK, v = res)
    }
  }
}
