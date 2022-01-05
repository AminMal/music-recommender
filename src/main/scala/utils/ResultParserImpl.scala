package scommender
package utils

import models.{SongDTO, User}

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.functions._
import utils.box.Box


class ResultParserImpl(dataFrameProvider: DataFrameProvider) extends ResultParser {

  override def getUserInfo(userId: Int): Option[User] =
    Box {
      User.fromRow(
        dataFrameProvider.usersDF.filter(_.getAs[Long]("user_id") == userId.toLong)
          .head()
      )
    }.toOption

  override def getSongDTOs(predictions: Seq[Rating]): Seq[SongDTO] = {
    dataFrameProvider.songsDF.filter(col("song_id") isin (predictions.map(_.product): _*))
      .collect().toSeq
      .map(SongDTO.fromRow)
  }

}
