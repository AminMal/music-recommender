package scommender
package utils

import utils.DataFrameSchemas._
import utils.box.{BoxF, BoxSupport}
import conf.{RecommenderDataPaths => Paths}

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.concurrent.{ExecutionContext, Future}


class DataFrameProvider(sparkSession: SparkSession) { self: BoxSupport =>


  /** reads training data dataframe.
   *
   * @return training dataframe
   */
  def trainingDF: DataFrame = sparkSession.read
    .schema(ratingsStruct)
    .parquet(Paths.trainPath)

  /** reads rating data dataframe.
   *
   * @return rating dataframe
   */
  def ratingsDF: DataFrame = sparkSession.read
    .schema(ratingsStruct)
    .parquet(Paths.ratingsPath)

  /** reads songs data dataframe
   *
   * @return songs dataframe
   */
  def songsDF: DataFrame = sparkSession.read
    .schema(songsStruct)
    .parquet(Paths.songsPath)

  /** reads users data dataframe
   *
   * @return users dataframe
   */
  def usersDF: DataFrame = sparkSession.read
    .schema(usersSchema)
    .parquet(Paths.usersPath)

  /** reads test data dataframe
   *
   * @return test dataframe
   */
  def testDataDF: DataFrame = sparkSession.read
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
      Future.successful {
        sparkSession.sparkContext.parallelize(trainingDF.collect().map(rowToRating))
      }
    }
  }

  /** reads rating dataframe, and converts it to BoxF instance of rating data RDD.
   *
   * @param ec execution context to perform time tracking
   * @return rating data rdd
   */
  def ratingRddBoxF(implicit ec: ExecutionContext): BoxF[RDD[Rating]] =
    toBoxF {
      Future.successful {
        sparkSession.sparkContext
          .parallelize(ratingsDF.collect().map(rowToRating))
      }
    }

}

object DataFrameProvider {
  def apply(sparkSession: SparkSession): DataFrameProvider =
    new DataFrameProvider(sparkSession) with BoxSupport

  def producer(sparkSession: SparkSession): () => DataFrameProvider =
    () => apply(sparkSession)
}