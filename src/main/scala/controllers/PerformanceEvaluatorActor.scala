package scommender
package controllers

import evaluation.EvaluationMode.EvaluationMode
import evaluation._
import utils.DataFrameProvider
import utils.box.BoxSupport

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.DataFrame
import models.ModelPerformanceResult

import service.DiagnosticsService

/**
 * This actor takes the responsibility of evaluating models based on whatever evaluation method that is requested.
 */
class PerformanceEvaluatorActor(
                               dataFrameProvider: DataFrameProvider
                               ) extends Actor with ActorLogging with BoxSupport {

  import PerformanceEvaluatorActor.Messages._

  private def parseEvaluationResult(results: Seq[DataFrame]): ModelPerformanceResult = {
    val rmseResult: DataFrame = results(1)
    val precisionAndRecallResult = results(2)
    val fmeasureResult = results(3)
    ModelPerformanceResult(
      precision = BigDecimal(precisionAndRecallResult.head().getAs[Double]("precision")),
      recall = BigDecimal(precisionAndRecallResult.head().getAs[Double]("recall")),
      fmeasure = BigDecimal(fmeasureResult.head().getAs[Double]("fMeasure")),
      rmse = BigDecimal(rmseResult.head().getAs[Double]("rmse"))
    )
  }

  def receive: Receive = {

    case EvaluateUsingAllMethods(model, mode) =>

      val ratings = dataFrameProvider.ratingsDF
      val testData = dataFrameProvider.testDataDF

      val shuffledEvaluator = new ShuffledEvaluation(
        ratings, testData
      )
      val rmseEvaluator = RmseEvaluation.fromShuffled(shuffledEvaluator)
      val precisionAndRecallEvaluator = PrecisionRecallEvaluator.fromShuffled(shuffledEvaluator, threshold = 0.65)

      val fMeasureEvaluator = FMeasureEvaluation.fromPrecisionRecall(precisionAndRecallEvaluator)

      val evaluationMethods: Seq[EvaluationMethod] = Seq(
        shuffledEvaluator, rmseEvaluator, precisionAndRecallEvaluator, fMeasureEvaluator
      )

      val methodsAndEvaluationResults = evaluationMethods.map(method => (method, method.evaluate(model)))
      mode match {
        case scommender.evaluation.EvaluationMode.FireAndForget =>
          methodsAndEvaluationResults foreach {
            case (method, evaluationResult) =>
              println(s"results for metrics: ${method.metric.toString} is:")
              println("----------------------------------------------------")
              evaluationResult.show(100)
          }
        case scommender.evaluation.EvaluationMode.Wait =>
          sender() ! toBox(methodsAndEvaluationResults.map(_._2))
      }
      val performanceResult = parseEvaluationResult(methodsAndEvaluationResults.map(_._2))
      DiagnosticsService.updateSessionPerformanceResultsForReport(model, performanceResult)
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
  def props(dataFrameProvider: DataFrameProvider): Props = Props(new PerformanceEvaluatorActor(dataFrameProvider))

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