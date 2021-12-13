package ir.ac.usc
package evaluation

import Bootstrap.spark

import evaluation.MetricsEnum.MetricsEnum
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions._


class ShuffledEvaluation(
                          val trainingPercentage: Double,
                          val testingPercentage: Double,
                          val ratings: DataFrame
                        ) extends EvaluationMethod {

  require(trainingPercentage > 0 && testingPercentage > 0, "train and test percentage must be both greater than 0")

  // overridden as 'val' since randomSplit is used
  override val data: (DataFrame, DataFrame) = {
    val Array(train, test) = ratings.randomSplit(Array(trainingPercentage, testingPercentage))
    (train, test)
  }

  override val trainData: DataFrame = data._1

  override val testData: DataFrame = data._2

  override val metric: MetricsEnum = MetricsEnum.Shuffled

  private def predictRatings(model: MatrixFactorizationModel): RDD[Rating] = {
    val usersProduct = testData.collect().map(row => row.getAs[Long]("user_id").toInt -> row.getAs[Long]("song_id").toInt)
    model.predict(spark.sparkContext.parallelize(usersProduct))
  }

  private def rowToRating(row: Row): Rating = Rating(
    user = row.getAs[Long]("user_id").toInt,
    product = row.getAs[Long]("song_id").toInt,
    rating = row.getAs[Double]("target")
  )

  private def getActualRating(userId: Long, songId: Long): Option[Rating] = {
      Some(rowToRating {
        ratings.filter(col("user_id") === userId and col("song_id") === songId).first()
      })
  }

  def evaluate(model: MatrixFactorizationModel): DataFrame = {
    val predictions = predictRatings(model)
    val predictionsDF = spark.createDataFrame(predictions)
    predictionsDF
      .join(
        right = ratings,
        joinExprs = predictionsDF.col("user") === ratings.col("user_id") and
          predictionsDF.col("product") === ratings.col("song_id")
      )
      .select(
        predictionsDF.col("user"),
        predictionsDF.col("product"),
        predictionsDF.col("rating"),
        ratings.col("target")
      )
  }
}

object ShuffledEvaluation {
  def apply(
             ratings: DataFrame,
             percentages: (Double, Double)
           ): ShuffledEvaluation = new ShuffledEvaluation(
    percentages._1, percentages._2, ratings
  )

  def apply(
             ratings: DataFrame,
             trainToTestFactor: Double
           ): ShuffledEvaluation = new ShuffledEvaluation(
    trainToTestFactor, 1.toDouble, ratings
  )

}