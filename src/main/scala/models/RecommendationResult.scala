package ir.ac.usc
package models


/**
 * Result of recommendation process.
 *
 * @param userId the id of the user that this recommendation is made for
 * @param songs  songs that this user probably likes
 * @param meta   metadata providing some data about the count of result
 */
case class RecommendationResult(
                                 userId: Int,
                                 songs: Seq[SongDTO],
                                 meta: Meta
                               ) {
  def this(userId: Int, songs: Seq[SongDTO], count: Int) = this(userId, songs, Meta(count))

  def this(userId: Int, songs: Seq[SongDTO]) = this(userId, songs, songs.length)
}

/**
 * This class holds some metadata about recommendation results (currently only holds count)
 *
 * @param count amount of result data.
 */
case class Meta(count: Int)