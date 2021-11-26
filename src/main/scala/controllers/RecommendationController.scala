package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import models.Song
import utils.ResultsHelper

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import HttpServer.newRecommenderActor
import controllers.RecommendationController.Responses.RecommendationResult

import akka.http.scaladsl.server.Route
import akka.pattern.ask
import controllers.RecommendationController.defaultTrendingSongs

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.Future

class RecommendationController extends Actor with ActorLogging {

  import RecommendationController.Messages._
  import RecommendationController.Responses._

  val resultsHelper = new ResultsHelper()

  override def receive: Receive = initialReceive

  val defaultResult: Long => RecommendationResult = userId => RecommendationResult(
    userId = userId,
    songs = defaultTrendingSongs,
    actorName = self.path.name
  )

  def initialReceive: Receive = {
    case UpdateContext(model) =>
      context.become(receiveWithModel(model))
      log.info(s"update factorization model in recommender actor")

    case GetRecommendations(userId, count) =>
      sender() ! RecommendationResult(
        userId = userId,
        songs = defaultTrendingSongs.take(count),
        actorName = self.path.name
      )
      self ! PoisonPill
  }

  def receiveWithModel(model: MatrixFactorizationModel): Receive = {
    case UpdateContext(newModel) =>
      log.info(s"update factorization model in recommender actor")
      context.become(receiveWithModel(newModel))

    case GetRecommendations(userId, count) =>
      val userOpt = resultsHelper.getUserInfo(userId)
      val recommendationResult = userOpt.map { user =>
        log.info(s"Found user: $user")
        val recommendations = model.recommendProducts(user.userId, count)
        log.info(s"Got ratings: ${recommendations.toSeq}")
        val songs = recommendations.map(rating => resultsHelper.getSongInfo(rating.product))

        RecommendationResult(
          userId = userId,
          songs = songs.filter(_.isDefined).map(_.get),
          actorName = self.path.name
        )
      }.getOrElse(defaultResult(userId.toLong))

      sender() ! recommendationResult
      self ! PoisonPill
  }

}

object RecommendationController {
  object Messages {
    case class UpdateContext(model: MatrixFactorizationModel)
    case class GetRecommendations(userId: Int, count: Int = 6)
  }
  object Responses {
    case class RecommendationResult(
                                   userId: Long,
                                   actorName: String,
                                   songs: Seq[Song]
                                   )
  }

  import Bootstrap.JsonImplicits._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val defaultTrendingSongs: Seq[Song] = Seq(
    Song(id = 125323, name = "Coloratura", Some("Coldplay")),
    Song(id = 321534, name = "Do I wanna know?", Some("Arctic monkeys")),
    Song(id = 413416, name = "Love and hate", Some("Michael Kiwanuka")),
    Song(id = 782351, name = "Riders on the storm", Some("The doors")),
    Song(id = 213632, name = "Lucky town", Some("Bruce Springsteen")),
    Song(id = 783294, name = "Paragon", Some("Soen"))
  )

  import Messages._
  import Bootstrap.system.dispatcher

  def routes(implicit timeout: Timeout): Route = path("recommend" / IntNumber) { userId =>
    parameter("count".as[Int].withDefault(6)) { count =>
      val recommenderFuture: Future[ActorRef] = newRecommenderActor
      val result = recommenderFuture.map { recommender =>
        (recommender ? GetRecommendations(userId, count))
          .mapTo[RecommendationResult]
      }.flatten

      onSuccess(result) { res =>
        complete(status = StatusCodes.OK, v = res)
      }
    }
  }
}
