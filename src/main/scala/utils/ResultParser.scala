package ir.ac.usc
package utils

import models.{SongDTO, User}

import org.apache.spark.mllib.recommendation.Rating


/**
 * This trait enables converting recommendation results into actual users and songs
 */
trait ResultParser {

  /**
   * get user object from user id
   *
   * @param userId user id
   * @return the actual user, which is optional
   */
  def getUserInfo(userId: Int): Option[User]

  /**
   * get songs from recommendation result
   *
   * @param predictions recommender predictions
   * @return songs fetched from song ids
   */
  def getSongDTOs(predictions: Seq[Rating]): Seq[SongDTO]

}
