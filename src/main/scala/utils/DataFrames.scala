package scommender
package utils

import Bootstrap.spark
import conf.{RecommenderDataPaths => Paths}
import utils.DataFrameSchemas._

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import utils.box.{BoxF, BoxSupport}
import utils.TimeUtils._

import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}


/**
 * Singleton object that has methods to read all domain dataframes
 */
object DataFrames {

  import BoxSupport._

  /** reads training data dataframe.
   *
   * @return training dataframe
   */
  def trainingDF: DataFrame = spark.read
    .schema(ratingsStruct)
    .parquet(Paths.trainPath)

  /** reads rating data dataframe.
   *
   * @return rating dataframe
   */
  def ratingsDF: DataFrame = spark.read
    .schema(ratingsStruct)
    .parquet(Paths.ratingsPath)

  /** reads songs data dataframe
   *
   * @return songs dataframe
   */
  def songsDF: DataFrame = spark.read
    .schema(songsStruct)
    .parquet(Paths.songsPath)

  /** reads users data dataframe
   *
   * @return users dataframe
   */
  def usersDF: DataFrame = spark.read
    .schema(usersSchema)
    .parquet(Paths.usersPath)

  /** reads test data dataframe
   *
   * @return test dataframe
   */
  def testDataDF: DataFrame = spark.read
    .schema(testStruct)
    .parquet(Paths.testPath)


  private val rowToRating: Row => Rating =
    row => {
      val userId = row.getLong(0).toInt
      val songId = row.getLong(1).toInt
      val target = row.getDouble(2)

      Rating(
        user = userId,
        product = songId,
        rating = target
      )
  }

  /** reads training dataframe, and converts it to BoxF instance of training data RDD.
   *
   * @param ec execution context to perform timetracking
   * @return training data rdd.
   */
  def trainRddBoxF(implicit ec: ExecutionContext): BoxF[RDD[Rating]] = {
    toBoxF {
      timeTrackFuture(operationName = Some("loading training rdd"), ChronoUnit.MILLIS) {
        Future.successful {
          spark.sparkContext.parallelize(trainingDF.collect().map(rowToRating))
        }
      }
    }
  }

  /** reads rating dataframe, and converts it to BoxF instance of rating data RDD.
   *
   * @param ec execution context to perform timetracking
   * @return rating data rdd
   */
  def ratingRddBoxF(implicit ec: ExecutionContext): BoxF[RDD[Rating]] =
    toBoxF {
      timeTrackFuture(operationName = Some("loading ratings rdd"), ChronoUnit.MILLIS) {
        Future.successful {
          spark.sparkContext
            .parallelize(ratingsDF.collect().map(rowToRating))
        }
      }
    }
}
