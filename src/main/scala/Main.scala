package ir.ac.usc

import Bootstrap.DataFrames.ratingsDF
import utils.{ALSBuilder, ResultsHelper}
import conf.ALSDefaultConf

import org.apache.spark.mllib.recommendation.Rating

import scala.util.Try


object Main extends App {

  val ratingsRDD = ratingsDF.rdd.map { ratingRow =>
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

  def getRecommendationsForUser(userId: Int, recommendationsCount: Int = 6): Option[Seq[Rating]] =
    Try[Seq[Rating]](
      model.recommendProducts(user = userId, num = recommendationsCount)
    ).toOption


  val res1 = getRecommendationsForUser(162731586)
    .map(_.map(ResultsHelper.getRecommendationResult).filter(_.isDefined).map(_.get))
  val res2 = getRecommendationsForUser(1426732356)
    .map(_.map(ResultsHelper.getRecommendationResult).filter(_.isDefined).map(_.get))
  val res3 = getRecommendationsForUser(1128888321)
    .map(_.map(ResultsHelper.getRecommendationResult).filter(_.isDefined).map(_.get))

  res1.foreach(_.foreach(println))
  res2.foreach(_.foreach(println))
  res3.foreach(_.foreach(println))


}
