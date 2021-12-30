package ir.ac.usc
package evaluation

import Bootstrap.spark
import evaluation.MetricsEnum.MetricsEnum

import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame


/**
 * Instances of this class, calculate base data (meaning userId, songId, ratings and prediction) that other subclasses
 * can use to perform evaluations
 *
 * @param ratings  original ratings
 * @param testData data to test model with
 */
class ShuffledEvaluation(
                          val ratings: DataFrame,
                          val testData: DataFrame
                        ) extends EvaluationMethod {

  override val metric: MetricsEnum = MetricsEnum.Shuffled

  private def predictRatings(model: MatrixFactorizationModel): RDD[Rating] = {
    val usersProduct = testData.collect().map(row => row.getAs[Long]("user_id").toInt -> row.getAs[Long]("song_id").toInt)
    model.predict(spark.sparkContext.parallelize(usersProduct))
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