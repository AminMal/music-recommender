package ir.ac.usc
package evaluation

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

case class ShuffledEvaluation private(
                                       trainingPercentage: Double,
                                       testingPercentage: Double,
                                       ratings: DataFrame
                                     ) extends EvaluationMethod {

  require(trainingPercentage > 0 && testingPercentage > 0, "train and test percentage must be both greater than 0")

  // overridden as 'val' since randomSplit is used
  override val data: (DataFrame, DataFrame) = {
    val Array(train, test) = ratings.randomSplit(Array(trainingPercentage, testingPercentage))
    (train, test)
  }

  override val trainData: DataFrame = data._1

  override val testData: DataFrame = data._2

  def evaluate(model: MatrixFactorizationModel) = {

  }
}

object ShuffledEvaluation {
  def apply(
             ratings: DataFrame,
             percentages: (Double, Double)
           ): ShuffledEvaluation = ShuffledEvaluation(
    percentages._1, percentages._2, ratings
  )

  def apply(
             ratings: DataFrame,
             trainToTestFactor: Double
           ): ShuffledEvaluation = ShuffledEvaluation(
    trainToTestFactor, 1.toDouble, ratings
  )

}