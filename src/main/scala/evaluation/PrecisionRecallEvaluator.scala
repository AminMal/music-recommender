package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._


/**
 * Instances of this class can evaluate precision and recall for a model.
 *
 * @param ratings   original ratings
 * @param testData  data to test model with
 * @param threshold predictions greater than this threshold are behaved as positives
 */
class PrecisionRecallEvaluator(
                                override val ratings: DataFrame,
                                override val testData: DataFrame,
                                val threshold: Double
                              ) extends ShuffledEvaluation(ratings, testData) {

  override val metric: MetricsEnum = MetricsEnum.PrecisionRecall

  override def evaluate(model: MatrixFactorizationModel): DataFrame = {
    val combinedRatings = super.evaluate(model)
    val dataWithState = combinedRatings.withColumn("threshold", expr(s"$threshold"))
      .withColumn("state", expr("getstate(rating, target, threshold)"))

    val truePositiveCount = dataWithState.filter(col("state") === "TP").count()
    val trueNegativeCount = dataWithState.filter(col("state") === "TN").count()
    val falsePositiveCount = dataWithState.filter(col("state") === "FP").count()
    val falseNegativeCount = dataWithState.filter(col("state") === "FN").count()
    import Bootstrap.spark.implicits._

    Seq((truePositiveCount, falsePositiveCount, trueNegativeCount, falseNegativeCount))
      .toDF(PrecisionRecallEvaluator.dfColNames: _*)
      .withColumn("precision", expr("TP / (TP + FP)"))
      .withColumn("recall", expr("TP / (TP + FN)"))
  }

}

object PrecisionRecallEvaluator {

  val dfColNames: Seq[String] = Seq("TP", "FP", "TN", "FN")

  def fromShuffled(shuffledEvaluation: ShuffledEvaluation, threshold: Double): PrecisionRecallEvaluator =
    new PrecisionRecallEvaluator(
      ratings = shuffledEvaluation.ratings,
      testData = shuffledEvaluation.testData,
      threshold = threshold
    )
}
