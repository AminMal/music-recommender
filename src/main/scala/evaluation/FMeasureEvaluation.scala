package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.expr


/**
 * Instances of this class can calculate f-measure (or f-score) of a model.
 *
 * @param ratings   dataframe of original ratings
 * @param testData  data to test model with
 * @param threshold predictions greater than this threshold are behaved as positives.
 */
class FMeasureEvaluation(
                          override val ratings: DataFrame,
                          override val testData: DataFrame,
                          override val threshold: Double
                        ) extends PrecisionRecallEvaluator(
  ratings = ratings,
  testData = testData,
  threshold = threshold
) {

  override val metric: MetricsEnum = MetricsEnum.FMeasure

  override def evaluate(model: MatrixFactorizationModel): DataFrame = {
    val precisionRecallData = super.evaluate(model)
    precisionRecallData.select(
      expr("fMeasure(precision, recall)") as "fMeasure"
    )
  }

}

object FMeasureEvaluation {
  def fromPrecisionRecall(evaluator: PrecisionRecallEvaluator): FMeasureEvaluation =
    new FMeasureEvaluation(
      ratings = evaluator.ratings,
      testData = evaluator.testData,
      threshold = evaluator.threshold
    )
}
