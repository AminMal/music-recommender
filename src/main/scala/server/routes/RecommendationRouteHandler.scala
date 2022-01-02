package scommender
package server.routes

import service.algebra.RecommendationServiceAlgebra
import utils.ApplicationJsonSupport
import utils.box.{BoxSupport, BoxToResponseSupport}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

/**
 * This class handles http requests for recommendation manager actor
 *
 * @param recommendationService recommendation service
 */
class RecommendationRouteHandler(
                                  recommendationService: RecommendationServiceAlgebra
                                )(implicit ec: ExecutionContext) extends BoxToResponseSupport {

  val route: Route = path("recommend" / IntNumber) { userId =>
    parameter("count".as[Int].withDefault(6)) { count =>

      recommendationService.getRecommendations(userId, count)

    }
  }

}