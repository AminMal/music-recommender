package ir.ac.usc
package server.routes

import service.algebra.RecommendationServiceAlgebra

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport

class RecommendationRouteHandler(recommendationService: RecommendationServiceAlgebra) {

  import RecommendationRouteHandler._

  val route: Route = path("recommend" / IntNumber) { userId =>
    parameter("count".as[Int].withDefault(6)) { count =>
      val result = recommendationService.getRecommendations(userId, count)

      onSuccess(result) { res =>
        complete(status = StatusCodes.OK, v = res)
      }
    }
  }

}

object RecommendationRouteHandler extends ApplicationJsonSupport