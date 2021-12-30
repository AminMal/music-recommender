package scommender
package utils

import models.{SongDTO, User}
import utils.DataFrames._

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.functions._

import scala.util.Try

class ResultParserImpl extends ResultParser {

  override def getUserInfo(userId: Int): Option[User] =
    Try {
      User.fromRow(
        usersDF.filter(_.getAs[Long]("user_id") == userId.toLong)
          .head()
      )
    }.toOption

  override def getSongDTOs(predictions: Seq[Rating]): Seq[SongDTO] = {
    songsDF.filter(col("song_id") isin (predictions.map(_.product): _*))
      .collect().toSeq
      .map(SongDTO.fromRow)
  }

}
