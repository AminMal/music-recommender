package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import models.{RecommendationResult, SongDTO}
import utils.{ResultParser, ResultParserImpl}
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
          resultParser.getSongDTOs(recommendations)
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

  val defaultTrendingSongs: Seq[SongDTO] = Seq(
    SongDTO.mock(id = 125323L, name = "Coloratura", artistName = "Coldplay"),
    SongDTO.mock(id = 321534L, name = "Do I wanna know?", artistName = "Arctic monkeys"),
    SongDTO.mock(id = 413416L, name = "Love and hate", artistName = "Michael Kiwanuka"),
    SongDTO.mock(id = 782351L, name = "Riders on the storm", artistName = "The doors"),
    SongDTO.mock(id = 213632L, name = "Lucky town", artistName = "Bruce Springsteen"),
    SongDTO.mock(id = 783294L, name = "Paragon", artistName = "Soen")
  )
}
