package ir.ac.usc

import Bootstrap._
import utils.{ALSBuilder, RecommenderDataPaths => Paths}

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.Row

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.functions._


object Main extends App {

  case class Song(
                 id: Long,
                 name: String,
                 artistName: Option[String]
                 )

  object Song {
    def fromRow(r: Row): Song = Song(
      id = r.getString(0).toLong,
      name = r.getString(1),
      artistName = Try(r.getString(2)).toOption
    )
  }

  val ratings = spark.read
    .option("header", "true")
    .csv(path = Paths.ratingsPath)

  ratings.show(10)

  val ratingsRDD = ratings.rdd.map { ratingRow =>
    val userId = ratingRow.getString(0).toInt
    val songId = ratingRow.getString(1).toInt
    val target = ratingRow.getString(2)

    Rating(
      user = userId,
      product = songId,
      rating = target.toDouble
    )
  }

  val model = ALSBuilder.forConfig(ALSDefaultConf).run(ratingsRDD)

  val songsDF = spark.read
    .option("header", "true")
    .csv(path = Paths.songsPath)

  val songsThisUserLikes = ratings.filter("user_id = 2032245261")

  songsThisUserLikes.show(100)

  def getSongInfo(songId: Int) = {
    Song.fromRow(
    songsDF.filter(s"song_id = $songId")
      .select(
        col("song_id"),
        col("name"),
        col("artist_name")
      )
      .head()
    )
  }


  Try(model.recommendProducts(2032245261, 6)) match {
    case Failure(exception) =>
      println("exception happened for user 2032245261")
      println("----------------------------")
      exception.printStackTrace()
      println("----------------------------")
    case Success(value) =>
      value.foreach { rating =>
        val song = getSongInfo(rating.product)
        println(
          s"""
             |User with id ${rating.user} will like song:
             |$song with rate of ${rating.rating}
             |""".stripMargin
        )
      }
  }


}
