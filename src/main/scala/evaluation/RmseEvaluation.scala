package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class RmseEvaluation(
                           override val ratings: DataFrame,
                           override val trainingPercentage: Double,
                           override val testingPercentage: Double
                         ) extends ShuffledEvaluation(trainingPercentage, testingPercentage, ratings) {
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
      trainingPercentage = shuffledEvaluation.trainingPercentage,
      testingPercentage = shuffledEvaluation.testingPercentage
    )
}
