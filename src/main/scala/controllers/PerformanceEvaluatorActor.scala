package scommender
package controllers

import evaluation.EvaluationMode.EvaluationMode
import evaluation._
import utils.DataFrames
import utils.box.BoxSupport

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

/**
 * This actor takes the responsibility of evaluating models based on whatever evaluation method that is requested.
 */
class PerformanceEvaluatorActor extends Actor with ActorLogging with BoxSupport {

  import PerformanceEvaluatorActor.Messages._

  def receive: Receive = {

    case EvaluateUsingAllMethods(model, mode) =>

      val ratings = DataFrames.ratingsDF
      val testData = DataFrames.testDataDF

      val shuffledEvaluator = new ShuffledEvaluation(
        ratings, testData
      )
      val rmseEvaluator = RmseEvaluation.fromShuffled(shuffledEvaluator)
      val precisionAndRecallEvaluator = PrecisionRecallEvaluator.fromShuffled(shuffledEvaluator, threshold = 0.65)

      val fMeasureEvaluator = FMeasureEvaluation.fromPrecisionRecall(precisionAndRecallEvaluator)

      val evaluationMethods: Seq[EvaluationMethod] = Seq(
        shuffledEvaluator, rmseEvaluator, precisionAndRecallEvaluator, fMeasureEvaluator
      )

      mode match {
        case EvaluationMode.FireAndForget =>
          evaluationMethods.foreach { method =>
            println(s"results for metrics: ${method.metric.toString} is:")
            println("----------------------------------------------------")
            method.evaluate(model).show(100)
          }

        case EvaluationMode.Wait =>
          val evaluationDataFrames = toBox(evaluationMethods.map(_.evaluate(model)))
          sender() ! evaluationDataFrames
      }
      self ! PoisonPill


    case EvaluationRequest(mode, method, model) =>
      val result = toBox(method.evaluate(model))

      mode match {
        case EvaluationMode.FireAndForget =>
          result.foreach(_.show(100))
        case EvaluationMode.Wait =>
          sender() ! result
      }
      self ! PoisonPill

  }

}

object PerformanceEvaluatorActor {
  /**
   * Generates performance evaluator actor Props in order to create new reference of this actor.
   *
   * @return props of this actor
   */
  def props: Props = Props[PerformanceEvaluatorActor]

  /**
   * Messages that this actor accepts
   */
  object Messages {
    case class EvaluationRequest(
                                  mode: EvaluationMode,
                                  method: EvaluationMethod,
                                  model: MatrixFactorizationModel
                                )

    case class EvaluateUsingAllMethods(
                                        model: MatrixFactorizationModel,
                                        mode: EvaluationMode = EvaluationMode.FireAndForget
                                      )
  }
}