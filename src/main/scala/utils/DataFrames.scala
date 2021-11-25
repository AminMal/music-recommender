package ir.ac.usc
package utils

import Bootstrap.spark

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import conf.{RecommenderDataPaths => Paths}
import DataFrameSchemas._


object DataFrames {

  def usersDF: DataFrame = spark.read
    .schema(usersSchema)
    .parquet(path = Paths.usersPath)

  def songsDF: DataFrame = spark.read
    .schema(songsStruct)
    .parquet(path = Paths.songsPath)

  def ratingsDF: DataFrame = spark.read
    .schema(ratingsStruct)
    .parquet(path = Paths.ratingsPath)

  def ratingsRDD: RDD[Rating] = ratingsDF.rdd.map { ratingRow =>
    val userId = ratingRow.getLong(0).toInt
    val songId = ratingRow.getLong(1).toInt
    val target = ratingRow.getDouble(2)

    Rating(
      user = userId,
      product = songId,
      rating = target
    )
  }

}
