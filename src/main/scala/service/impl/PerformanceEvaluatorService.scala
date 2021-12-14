package ir.ac.usc
package service.impl

import service.algebra.PerformanceEvaluatorServiceAlgebra

import akka.actor.ActorRef
import akka.util.Timeout
import evaluation.{EvaluationMethod, EvaluationMode}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import akka.pattern.ask
import controllers.PerformanceEvaluatorActor.Messages._

class PerformanceEvaluatorService(performanceEvaluator: () => ActorRef) extends PerformanceEvaluatorServiceAlgebra {

  implicit val timeout: Timeout = Timeout(50, TimeUnit.SECONDS)

  override def evaluate(model: MatrixFactorizationModel, method: EvaluationMethod): Future[DataFrame] = {
    (
      performanceEvaluator() ? EvaluationRequest(
      mode = EvaluationMode.Wait,
      model = model,
      method = method
      )
    )
      .mapTo[DataFrame]
  }

  override def evaluateDispatched(model: MatrixFactorizationModel, method: EvaluationMethod): Unit = {
    performanceEvaluator() ! EvaluationRequest(
      mode = EvaluationMode.FireAndForget,
      method = method, model = model
    )
  }

  override def evaluateUsingAllMethods(model: MatrixFactorizationModel): Future[Seq[DataFrame]] = {
    (performanceEvaluator() ? EvaluateUsingAllMethods(model, mode = EvaluationMode.Wait)).mapTo[Seq[DataFrame]]
  }

  override def evaluateUsingAllMethodsDispatched(model: MatrixFactorizationModel): Unit = {
    performanceEvaluator() ! EvaluateUsingAllMethods(model)
  }
}
