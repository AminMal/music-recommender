package ir.ac.usc
package service.impl

import controllers.PerformanceEvaluatorActor.Messages._
import evaluation.{EvaluationMethod, EvaluationMode}
import exception.ModelNotTrainedYetException
import service.algebra.{ContextManagerServiceAlgebra, PerformanceEvaluatorServiceAlgebra}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

class PerformanceEvaluatorService(
                                   performanceEvaluator: () => ActorRef,
                                   contextService: ContextManagerServiceAlgebra
                                 ) extends PerformanceEvaluatorServiceAlgebra {

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

  override def evaluateDefaultModel(method: EvaluationMethod): Future[DataFrame] = {
    contextService.getLatestModel.flatMap { modelOpt =>
      val model = modelOpt.getOrElse(throw ModelNotTrainedYetException)
      evaluate(model, method)
    }(ExecutionContext.Implicits.global)
  }
}
