package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._


/**
 * Isntances of this class can calculate root mean squared error of matrix factorization models.
 * @param ratings original ratings
 * @param testData data to test model with
 */
class RmseEvaluation(
                    ratings: DataFrame,
                    testData: DataFrame
                    ) extends ShuffledEvaluation(ratings, testData) {
  override val metric: MetricsEnum = MetricsEnum.RMSE

  override def evaluate(model: MatrixFactorizationModel): DataFrame = {
    val combinedRatings = super.evaluate(model)
    combinedRatings.withColumn("error", expr("geterr(rating, target)"))
      .select(sqrt(mean("error")) as "rmse")
  }
}

object RmseEvaluation {
  def fromShuffled(shuffledEvaluation: ShuffledEvaluation): RmseEvaluation =
    new RmseEvaluation(
      ratings = shuffledEvaluation.ratings,
      testData = shuffledEvaluation.testData
    )
}
