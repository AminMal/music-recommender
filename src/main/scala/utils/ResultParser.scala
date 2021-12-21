package ir.ac.usc
package utils

import models.{SongDTO, User}

import org.apache.spark.mllib.recommendation.Rating

trait ResultParser {

  def getUserInfo(userId: Int): Option[User]

  def getSongDTOs(predictions: Seq[Rating]): Seq[SongDTO]

}
