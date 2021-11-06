package ir.ac.usc
package utils


/**
 *    Application config object for paths containing data
 *      note that files included on my own computer in baseDir are excluded from git
 *      So if you want to use the application, please use your own files and change baseDir and other values here
 *      And everything should be fine
 */

object RecommenderDataPaths {

  private final val baseDir: String     = "src/main/resources/data/"
  val membersPath: String               = baseDir + "members.csv"
  val songsPath: String                 = baseDir + "songs.csv"
  val songsExtraInfoPath: String        = baseDir + "song_extra_info.csv"
  val ratingsPath: String               = baseDir + "train.csv"

}
