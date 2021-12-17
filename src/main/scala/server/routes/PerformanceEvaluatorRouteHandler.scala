package ir.ac.usc
package server.routes

import evaluation._
import service.algebra.PerformanceEvaluatorServiceAlgebra
import utils.{ApplicationJsonSupport, DataFrames}

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class PerformanceEvaluatorRouteHandler(
                                        performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra
                                      ) {

  import utils.Matchers._

  import PerformanceEvaluatorRouteHandler._

  val route: Route = pathPrefix("evaluations") {
    path("evaluate" / EvaluationMethod) { extractedMethod =>
      get {

        val ratings = DataFrames.ratingsDF
        val testData = DataFrames.testDataDF
        import Bootstrap.spark

        parameters(
          "threshold".as[Double].withDefault(0.65),
          "take".as[Double].withDefault(100)
        ) { (threshold, take) =>
          val method: EvaluationMethod = extractedMethod match {
            case MetricsEnum.RMSE =>
              new RmseEvaluation(ratings, testData)

            case MetricsEnum.Shuffled =>
              new ShuffledEvaluation(DataFrames.ratingsDF, DataFrames.testDataDF)

            case MetricsEnum.PrecisionRecall =>
              new PrecisionRecallEvaluator(ratings, testData, threshold)

            case MetricsEnum.FMeasure =>
              new FMeasureEvaluation(ratings, testData, threshold)
          }
          val result = performanceEvaluatorService.evaluateDefaultModel(method)

          onSuccess(result) { df =>
            complete(StatusCodes.OK, spark.createDataFrame(
              spark.sparkContext.parallelize(df.take(take)),
              schema = df.schema
            ).toDF(df.columns: _*))
          }

        }
      }
    }
  }

}

object PerformanceEvaluatorRouteHandler extends ApplicationJsonSupport