package ir.ac.usc
package service.algebra

import evaluation.EvaluationMethod

import utils.box.BoxF
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

import scala.concurrent.Future


/**
 * Service representing performance evaluator actor
 */
trait PerformanceEvaluatorServiceAlgebra {

  /**
   * evaluate a model based on an evaluation method (wait mode)
   * @param model model to evaluate
   * @param method method to evaluate with
   * @return resulting dataframe
   */
  def evaluate(model: MatrixFactorizationModel, method: EvaluationMethod): BoxF[DataFrame]

  /**
   * evaluate a model based on an evaluation method (fire and forget mode)
   * @param model model to evaluate
   * @param method method to evaluate with
   */
  def evaluateDispatched(model: MatrixFactorizationModel, method: EvaluationMethod): Unit

  /**
   * evaluate a model using all existing methods (wait mode)
   * @param model model to evaluate
   * @return all resulting dataframes
   */
  def evaluateUsingAllMethods(model: MatrixFactorizationModel): BoxF[Seq[DataFrame]]

  /**
   * evaluate a model using all existing methods (fire and forget mode)
   * @param model model to evaluate
   */
  def evaluateUsingAllMethodsDispatched(model: MatrixFactorizationModel): Unit

  /**
   * evaluate application latest model
   * @param method model to evaluate
   * @return
   */
  def evaluateDefaultModel(method: EvaluationMethod): BoxF[DataFrame]

}
