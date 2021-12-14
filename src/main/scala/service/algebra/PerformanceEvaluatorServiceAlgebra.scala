package ir.ac.usc
package service.algebra

import evaluation.EvaluationMethod
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

import scala.concurrent.Future

trait PerformanceEvaluatorServiceAlgebra {

  def evaluate(model: MatrixFactorizationModel, method: EvaluationMethod): Future[DataFrame]
  def evaluateDispatched(model: MatrixFactorizationModel, method: EvaluationMethod): Unit
  def evaluateUsingAllMethods(model: MatrixFactorizationModel): Future[Seq[DataFrame]]
  def evaluateUsingAllMethodsDispatched(model: MatrixFactorizationModel): Unit

}
