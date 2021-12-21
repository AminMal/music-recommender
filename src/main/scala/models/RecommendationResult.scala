package ir.ac.usc
package models

case class RecommendationResult(
                               userId: Int,
                               songs: Seq[SongDTO],
                               meta: Meta
                               ) {
  def this(userId: Int, songs: Seq[SongDTO], count: Int) = this(userId, songs, Meta(count))
  def this(userId: Int, songs: Seq[SongDTO]) = this(userId, songs, songs.length)
}

case class Meta(count: Int)