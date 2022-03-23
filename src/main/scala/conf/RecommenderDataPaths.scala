package scommender
package conf

/**
 * Application config object for paths containing data
 * note that files included on my own computer in baseDir are excluded from git
 * So if you want to use the application, please use your own files and change baseDir and other values here
 * And everything should be fine
 */

object RecommenderDataPaths {

  private final val baseDir: String = "src/main/resources/data/"
  val usersPath: String = baseDir + "users"
  val songsPath: String = baseDir + "songs"
  val ratingsPath: String = baseDir + "ratings"
  val trainPath: String = baseDir + "train"
  val testPath: String = baseDir + "test"
  val latestModelPath: String = baseDir + "latest_model"

}
