package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import models.Song
import utils.{ResultParser, ResultParserImpl}
import models.RecommendationResult
import controllers.RecommendationController.defaultTrendingSongs

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import java.time.temporal.ChronoUnit


class RecommendationController(resultParser: ResultParser) extends Actor with ActorLogging {

  import RecommendationController.Messages._
  import utils.Common.timeTrack
  override def receive: Receive = initialReceive

  val defaultResult: Int => RecommendationResult = userId =>
    new RecommendationResult(
      userId = userId, songs = defaultTrendingSongs
    )

  def initialReceive: Receive = {
    case UpdateContext(model) =>
      context.become(receiveWithModel(model))
      log.info(s"update factorization model in recommender actor")

    case GetRecommendations(userId, count) =>
      sender() ! new RecommendationResult(
        userId = userId,
        songs = defaultTrendingSongs.take(count)
      )
      self ! PoisonPill
  }

  def receiveWithModel(model: MatrixFactorizationModel): Receive = {
    case GetRecommendations(userId, count) =>
      val userOpt = timeTrack {
        resultParser.getUserInfo(userId)
      } (operationName = Some("Get user info"), ChronoUnit.MILLIS)

      val recommendationResult = userOpt.map { user =>
        log.info(s"Found user: $user")

        val recommendations = timeTrack {
          model.recommendProducts(user.userId, count)
        }(operationName = Some("Getting recommendations from model"), ChronoUnit.MILLIS)

        log.info(s"Got ratings: ${recommendations.toSeq}")

        val songs = timeTrack {
          resultParser.getSongs(recommendations.map(_.product))
        }(operationName = Some("Getting song info from recommendation result"), ChronoUnit.MILLIS)

        new RecommendationResult(
          userId = userId,
          songs = songs
        )
      }.getOrElse(defaultResult(userId))

      sender() ! recommendationResult
      self ! PoisonPill
  }

}

object RecommendationController {

  def props: Props = Props(new RecommendationController(new ResultParserImpl()))

  object Messages {
    case class UpdateContext(model: MatrixFactorizationModel)
    case class GetRecommendations(userId: Int, count: Int = 6)
  }

  val defaultTrendingSongs: Seq[Song] = Seq(
    Song(id = 125323, name = "Coloratura", Some("Coldplay")),
    Song(id = 321534, name = "Do I wanna know?", Some("Arctic monkeys")),
    Song(id = 413416, name = "Love and hate", Some("Michael Kiwanuka")),
    Song(id = 782351, name = "Riders on the storm", Some("The doors")),
    Song(id = 213632, name = "Lucky town", Some("Bruce Springsteen")),
    Song(id = 783294, name = "Paragon", Some("Soen"))
  )
}
