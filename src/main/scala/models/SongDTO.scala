package ir.ac.usc
package models

case class SongDTO(
                  id: Long,
                  name: String,
                  length: Long,
                  genreIds: String,
                  artistName: String,
                  language: Double // lack of good data model
                  ) {
  def decoupled() = (id, name, length, genreIds, artistName, language)
}

object SongDTO {
  val dfColNames = Seq("song_id", "name", "length", "genre_ids", "artist_name", "language")
}
