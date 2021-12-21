package ir.ac.usc
package utils

import utils.DataFrames._
import models.{SongDTO, User}

import org.apache.spark.mllib.recommendation.Rating

import scala.util.Try
import org.apache.spark.sql.functions._

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
