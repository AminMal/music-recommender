package ir.ac.usc

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}
import conf.{RecommenderDataPaths => Paths}

object Bootstrap {

  Logger.getLogger("org").setLevel(Level.ERROR)

  lazy final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local")  // todo, this needs to be removed in production
    .getOrCreate()

  println("---initialized sprak session---")

  object DataFrames {
    val usersDF: DataFrame = spark.read
      .option("header", "true")
      .csv(path = Paths.usersPath)

    val songsDF: DataFrame = spark.read
      .option("header", "true")
      .csv(path = Paths.songsPath)

    val ratingsDF: DataFrame = spark.read
      .option("header", "true")
      .csv(path = Paths.ratingsPath)
  }

}
