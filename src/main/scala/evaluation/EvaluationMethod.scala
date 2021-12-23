package ir.ac.usc
package evaluation

import evaluation.MetricsEnum.MetricsEnum

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame


/**
 * The general interface that can evaluate MatrixFactorizationModel.
 */
trait EvaluationMethod {
  /**
   * This method evaluates the given input model, based on the logic that subclasses or instances provide.
   * @param model model to be evaluated
   * @return DataFrame of evaluation result
   */
  def evaluate(model: MatrixFactorizationModel): DataFrame

  /**
   * Metric name of the evaluation method (see MetricsEnum.scala)
   * @return metric enum
   */
  def metric: MetricsEnum
}
