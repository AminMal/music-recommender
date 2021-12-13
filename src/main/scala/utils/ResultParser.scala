package ir.ac.usc
package utils

import models.{Song, User}

trait ResultParser {

  def getSongInfo(songId: Int): Option[Song]

  def getUserInfo(userId: Int): Option[User]

}
