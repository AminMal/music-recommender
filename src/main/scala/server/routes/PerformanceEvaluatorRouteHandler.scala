package ir.ac.usc
package server.routes

import models.responses.{ErrorBody, FailureResponse}
import service.algebra.PerformanceEvaluatorServiceAlgebra

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport

class PerformanceEvaluatorRouteHandler(performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra) {

  import PerformanceEvaluatorRouteHandler._

  val route: Route = pathPrefix("evaluations") {
    complete(StatusCodes.NotImplemented, FailureResponse(
      error = ErrorBody.NotImplementedYet
    ))
  }

}

object PerformanceEvaluatorRouteHandler extends ApplicationJsonSupport