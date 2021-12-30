package scommender
package models

import org.apache.spark.sql.Row

case class SongDTO(
                    id: Long,
                    name: String,
                    length: Long,
                    genreIds: String,
                    artistName: String,
                    language: Double // lack of good data
                  )

object SongDTO {
  val dfColNames = Seq("song_id", "name", "length", "genre_ids", "artist_name", "language")

  def fromRow(row: Row): SongDTO = SongDTO(
    id = row.getAs[Long]("song_id"),
    name = row.getAs[String]("name"),
    length = row.getAs[Long]("length"),
    genreIds = row.getAs[String]("genre_ids"),
    artistName = row.getAs[String]("artist_name"),
    language = row.getAs[Double]("language")
  )

  def mock(id: Long, name: String, artistName: String): SongDTO = SongDTO(
    id, name, 0, "", artistName, -0.1
  )
}
