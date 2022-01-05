package scommender
package server.routes

import evaluation._
import service.algebra.PerformanceEvaluatorServiceAlgebra
import utils.box.BoxSupport
import utils.{ApplicationJsonSupport, DataFrameProvider}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import exception.NotImplementedYet

import scala.concurrent.ExecutionContext


/**
 * This class handles http requests for performance evaluator actor
 *
 * @param performanceEvaluatorService performance evaluator service
 */
class PerformanceEvaluatorRouteHandler(
                                        performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra,
                                        dataFrameProvider: DataFrameProvider
                                      )(implicit ec: ExecutionContext) extends BoxSupport {

  import utils.Matchers._

  import PerformanceEvaluatorRouteHandler._

  val route: Route = pathPrefix("evaluations") {
    path("evaluate" / EvaluationMethod) { extractedMethod =>
      get {

        val ratings = dataFrameProvider.ratingsDF
        val testData = dataFrameProvider.testDataDF
        import Bootstrap.spark

        parameters(
          "threshold".as[Double].withDefault(0.65),
          "take".as[Double].withDefault(100)
        ) { (threshold, take) =>
          val method: EvaluationMethod = extractedMethod match {
            case MetricsEnum.RMSE =>
              new RmseEvaluation(ratings, testData)

            case MetricsEnum.Shuffled =>
              new ShuffledEvaluation(ratings, testData)

            case MetricsEnum.PrecisionRecall =>
              new PrecisionRecallEvaluator(ratings, testData, threshold)

            case MetricsEnum.FMeasure =>
              new FMeasureEvaluation(ratings, testData, threshold)

            case notImplementedMetric =>
              throw NotImplementedYet(
                entityKey = "evaluation metric",
                entityValue = notImplementedMetric.toString
              )
          }
          val result = performanceEvaluatorService.evaluateDefaultModel(method)

          onSuccess(result.toScommenderResponse) { scommenderResponseDF =>
            complete(
              scommenderResponseDF.status,
              scommenderResponseDF.map { dataframe =>
                spark.createDataFrame(
                  spark.sparkContext.parallelize(dataframe.take(take)),
                  schema = dataframe.schema
                ).toDF(dataframe.columns: _*)
              }
            )
          }

        }
      }
    }
  }

}

object PerformanceEvaluatorRouteHandler extends ApplicationJsonSupport