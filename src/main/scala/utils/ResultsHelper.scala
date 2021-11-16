package ir.ac.usc
package utils

import Bootstrap.DataFrames._
import models.{RecommendationResult, Song, User}

import org.apache.spark.mllib.recommendation.Rating

import scala.util.Try

class ResultsHelper {

  def getSongInfo(songId: Int): Option[Song] =
    Try {
      Song.fromRow(
        songsDF.filter(s"song_id = $songId")
          .head()
      )
    }.toOption

  def getUserInfo(userId: Int): Option[User] =
    Try {
      User.fromRow(
        usersDF.filter(s"user_id = $userId")
          .head()
      )
    }.toOption

  def getRecommendationResult(rating: Rating): Option[RecommendationResult] =
    for {
      song <- getSongInfo(rating.product)
      user <- getUserInfo(rating.user)
    } yield RecommendationResult(user = user, song = song)

}
