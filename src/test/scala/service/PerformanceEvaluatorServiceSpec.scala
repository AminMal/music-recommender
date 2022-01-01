package scommender
package service

import conf.ALSDefaultConf
import evaluation.{FMeasureEvaluation, PrecisionRecallEvaluator, RmseEvaluation, ShuffledEvaluation}
import utils.{ALSBuilder, DataFrames}

import utils.box.{BoxF, BoxSupport}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.scalatest.Matchers

class PerformanceEvaluatorServiceSpec extends BoxFWordSpecLike with Matchers with BoxSupport {

  val provider = new ServiceProvider("performance-service")
  import provider._

  private val newModel: BoxF[MatrixFactorizationModel] = {
    DataFrames
      .trainRddBoxF
      .map(ratings => ALSBuilder.forConfig(ALSDefaultConf).run(ratings))
  }

  private val shuffledMethod = new ShuffledEvaluation(
    DataFrames.ratingsDF, DataFrames.testDataDF
  )

  private val precisionRecallEvaluator = PrecisionRecallEvaluator.fromShuffled(shuffledMethod, 0.65)

  private val rmseMethod = RmseEvaluation.fromShuffled(shuffledMethod)

  private val fMeasure = FMeasureEvaluation.fromPrecisionRecall(precisionRecallEvaluator)

  "performance evaluator service" should {
    "return predictions and original ratings using shuffled evaluation" inBox {

      val performanceDatasetBox = for {
        model <- newModel
        evaluation <- service.performanceEvaluatorService.evaluate(
          model, method = shuffledMethod
        )
      } yield evaluation

      performanceDatasetBox.map { result =>
        result.show(50)
        assert(result.columns sameElements Array("user", "product", "rating", "target"))
      }
    }

    "return precision and recall in PrecisionAndRecall evaluation mode" inBox {
      val futurePerformanceDataset = for {
        model <- newModel
        evaluation <- service.performanceEvaluatorService.evaluate(
          model, method = precisionRecallEvaluator
        )
      } yield evaluation

      futurePerformanceDataset.map { result =>
        result.show(1)
        assert(result.count() == 1 && (result.columns sameElements Array("TP", "FP", "TN", "FN", "precision", "recall")))
      }
    }

    "perform rmse evaluation in RmseEvaluation mode" inBox {
      val futurePerformanceDataset = for {
        model <- newModel
        evaluation <- service.performanceEvaluatorService.evaluate(
          model, method = rmseMethod
        )
      } yield evaluation

      futurePerformanceDataset.map { result =>
        result.show(1)
        assert((result.columns sameElements Array("rmse")) && result.count() == 1)
      }
    }

    "perform fMeasure evaluation in FMeasure Evaluation mode" inBox {
      val futurePerformanceDataset = for {
        model <- newModel
        evaluation <- service.performanceEvaluatorService.evaluate(
          model, method = fMeasure
        )
      } yield evaluation

      futurePerformanceDataset.map { result =>
        result.show()
        assert(result.count() == 1 && (result.columns sameElements Array("fMeasure")))
      }
    }
  }

}
