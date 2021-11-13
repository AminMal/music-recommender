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
  val songsExtraInfoPath: String        = baseDir + "song_extra_info.csv"
  val usersPath: String                 = baseDir + "users.csv"
  val songsPath: String                 = baseDir + "songs_main.csv"
  val ratingsPath: String               = baseDir + "ratings.csv"

}
