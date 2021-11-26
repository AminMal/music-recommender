package ir.ac.usc
package utils

import utils.DataFrames._
import models.{RecommendationResult, Song, User}

import org.apache.spark.mllib.recommendation.Rating

import scala.util.{Failure, Success, Try}

class ResultsHelper {

  def getSongInfo(songId: Int): Option[Song] = {
    Try {
      Song.fromRow(
        songsDF.filter(_.getLong(0) == songId.toLong)
          .head()
      )
    } match {
      case Failure(exception) =>
        println("----exception happened in fetching result----")
        println("-------------")
        exception.printStackTrace()
        println("-------------")
        Option.empty[Song]
      case Success(value) =>
        Some(value)
    }
  }

  def getUserInfo(userId: Int): Option[User] =
    Try {
      User.fromRow(
        usersDF.filter(s"user_id = ${userId.toLong}")
          .head()
      )
    }.toOption

  def getRecommendationResult(rating: Rating): Option[RecommendationResult] =
    for {
      song <- getSongInfo(rating.product)
      user <- getUserInfo(rating.user)
    } yield RecommendationResult(user = user, song = song)

}
