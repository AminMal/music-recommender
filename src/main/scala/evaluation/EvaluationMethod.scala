package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

trait EvaluationMethod {

  def trainData: DataFrame
  def testData: DataFrame
  def data: (DataFrame, DataFrame)

  def evaluate(model: MatrixFactorizationModel): DataFrame

  def metric: MetricsEnum
}
