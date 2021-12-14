package ir.ac.usc
package utils

import utils.DataFrames._
import models.{Song, User}

import scala.util.Try
import org.apache.spark.sql.functions._

class ResultParserImpl extends ResultParser {

  override def getSongInfo(songId: Int): Option[Song] = {
    Try {
      Song.fromRow(
        songsDF.filter(_.getAs[Long]("song_id") == songId.toLong)
          .head()
      )
    }.toOption
  }

  override def getUserInfo(userId: Int): Option[User] =
    Try {
      User.fromRow(
        usersDF.filter(_.getAs[Long]("user_id") == userId.toLong)
          .head()
      )
    }.toOption

  override def getSongs(songsIds: Seq[Int]): Seq[Song] = {
    songsDF.filter(col("song_id") isin (songsIds: _*))
      .collect().toSeq
      .map(Song.fromRow)
  }

}
