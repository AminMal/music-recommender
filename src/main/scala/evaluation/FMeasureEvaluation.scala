package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.expr

class FMeasureEvaluation(
                          override val trainingPercentage: Double,
                          override val testingPercentage: Double,
                          override val ratings: DataFrame,
                          override val threshold: Double
                        ) extends PrecisionRecallEvaluator(
  trainingPercentage = trainingPercentage,
  testingPercentage = testingPercentage,
  ratings = ratings,
  threshold = threshold
) {

  override val metric: MetricsEnum = MetricsEnum.FMeasure

  override def evaluate(model: MatrixFactorizationModel): DataFrame = {
    val precisionRecallData = super.evaluate(model)
    precisionRecallData.select(
      expr("fMeasure(precision, recall)")
    )
  }

}

object FMeasureEvaluation {
  def fromPrecisionRecall(evaluator: PrecisionRecallEvaluator): FMeasureEvaluation =
    new FMeasureEvaluation(
      trainingPercentage = evaluator.trainingPercentage,
      testingPercentage = evaluator.testingPercentage,
      ratings = evaluator.ratings,
      threshold = evaluator.threshold
    )
}
