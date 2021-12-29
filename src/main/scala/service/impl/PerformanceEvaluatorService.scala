package ir.ac.usc
package service.impl

import controllers.PerformanceEvaluatorActor.Messages._
import evaluation.{EvaluationMethod, EvaluationMode}
import exception.ModelNotTrainedYetException
import service.algebra.{ContextManagerServiceAlgebra, PerformanceEvaluatorServiceAlgebra}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import utils.box.{BoxF, BoxSupport}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame

import scala.concurrent.{ExecutionContext, Future}

class PerformanceEvaluatorService(
                                   performanceEvaluator: () => ActorRef,
                                   contextService: ContextManagerServiceAlgebra
                                 )(
    implicit timeout: Timeout,
    executionContext: ExecutionContext
  ) extends PerformanceEvaluatorServiceAlgebra with BoxSupport {

  override def evaluate(model: MatrixFactorizationModel, method: EvaluationMethod): BoxF[DataFrame] = {
    val evaluationRequest = EvaluationRequest(
      mode = EvaluationMode.Wait,
      model = model,
      method = method
    )
    performanceEvaluator() ??[DataFrame] evaluationRequest
  }

  override def evaluateDispatched(model: MatrixFactorizationModel, method: EvaluationMethod): Unit = {
    performanceEvaluator() ! EvaluationRequest(
      mode = EvaluationMode.FireAndForget,
      method = method, model = model
    )
  }

  override def evaluateUsingAllMethods(model: MatrixFactorizationModel): BoxF[Seq[DataFrame]] = {
    val evaluationRequest = EvaluateUsingAllMethods(model, mode = EvaluationMode.Wait)
    performanceEvaluator() ??[Seq[DataFrame]] evaluationRequest
  }

  override def evaluateUsingAllMethodsDispatched(model: MatrixFactorizationModel): Unit = {
    performanceEvaluator() ! EvaluateUsingAllMethods(model)
  }

  override def evaluateDefaultModel(method: EvaluationMethod): BoxF[DataFrame] = {
    contextService.getLatestModel.flatMap { modelOpt =>
      val model = modelOpt.getOrElse(throw ModelNotTrainedYetException)
      evaluate(model, method)
    }
  }
}
