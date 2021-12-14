package ir.ac.usc
package models

case class RecommendationResult(
                               userId: Int,
                               songs: Seq[Song],
                               meta: Meta
                               ) {
  def this(userId: Int, songs: Seq[Song], count: Int) = this(userId, songs, Meta(count))
  def this(userId: Int, songs: Seq[Song]) = this(userId, songs, songs.length)
}

case class Meta(count: Int)