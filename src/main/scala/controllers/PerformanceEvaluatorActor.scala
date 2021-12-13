package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, Props}
import evaluation.{EvaluationMethod, EvaluationMode, FMeasureEvaluation, PrecisionRecallEvaluator, RmseEvaluation, ShuffledEvaluation}
import evaluation.EvaluationMode.EvaluationMode

import akka.pattern.ask
import akka.util.Timeout
import controllers.ContextManagerActor.Messages.GetLatestModel

import utils.DataFrames
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import java.util.concurrent.TimeUnit
import scala.concurrent.Future

class PerformanceEvaluatorActor extends Actor with ActorLogging {

  import PerformanceEvaluatorActor.Messages._
  import HttpServer.contextManagerActor

  def receive: Receive = {

    case EvaluateUsingAllMethods(model) =>

      val ratings = DataFrames.ratingsDF

      val shuffledEvaluator = new ShuffledEvaluation(
        trainingPercentage = 0.75, testingPercentage = 0.25, ratings = ratings
      )
      val rmseEvaluator = RmseEvaluation.fromShuffled(shuffledEvaluator)
      val precisionAndRecallEvaluator = PrecisionRecallEvaluator.fromShuffled(shuffledEvaluator, threshold = 0.65)

      val fMeasureEvaluator = FMeasureEvaluation.fromPrecisionRecall(precisionAndRecallEvaluator)

      val evaluationMethods: Seq[EvaluationMethod] = Seq(
        shuffledEvaluator, rmseEvaluator, precisionAndRecallEvaluator, fMeasureEvaluator
      )

      evaluationMethods.foreach { method =>
        println(s"results for metrics: ${method.metric.toString} is:")
        println("----------------------------------------------------")
        method.evaluate(model).show(100)
      }

    case EvaluationRequest(mode, method) =>
      val modelFuture: Future[Option[MatrixFactorizationModel]] = {
        implicit val getModelTimeout: Timeout = Timeout(2, TimeUnit.SECONDS)
        (contextManagerActor ? GetLatestModel).mapTo[Option[MatrixFactorizationModel]]
      }

      modelFuture.foreach { modelOpt =>
        modelOpt.foreach { model =>
          val result = method.evaluate(model)
          if (mode == EvaluationMode.FireAndForget) {
            result
              .show(100)
          } else if (mode == EvaluationMode.Wait) {
            sender() ! result
          }
        }
      }(context.dispatcher)
  }

}

object PerformanceEvaluatorActor {
  object Messages {
    case class EvaluationRequest(
                                mode: EvaluationMode,
                                method: EvaluationMethod
                                )

    case class EvaluateUsingAllMethods(model: MatrixFactorizationModel)
  }

  def props: Props = Props[PerformanceEvaluatorActor]
}