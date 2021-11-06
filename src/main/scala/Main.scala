package ir.ac.usc

import Bootstrap._

import org.apache.spark.sql.functions._
import utils.{RecommenderDataPaths => Paths}

import org.apache.spark.sql.types.DataTypes


object Main extends App {

  val membersDF = spark.read
    .option("header", "true")
    .csv(path = Paths.membersPath)
    .select(
      col("msno") as "user_id",
      expr("coalesce(gender, 'male') as gender")
    )

  val songsBaseDF = spark.read
    .option("header", "true")
    .csv(path = Paths.songsPath)

  val songsExtraInfo = spark.read
    .option("header", "true")
    .csv(path = Paths.songsExtraInfoPath)
    .select(
      col("song_id") as "s_id",
      col("name") as "song_name"
    )

  val songsDF = songsBaseDF
    .join(songsExtraInfo, songsExtraInfo("s_id") === songsBaseDF("song_id"), joinType = "left")
    .select(
      col("song_id"),
      col("genre_ids"),
      col("song_name"),
      col("artist_name")
    )

  val ratings = spark.read
    .option("header", "true")
    .csv(path = Paths.ratingsPath)
    .select(
      col("msno") as "user_id",
      col("song_id"),
      col("target").cast(DataTypes.BooleanType) as "liked"
    )

  ratings.show(20)


}
