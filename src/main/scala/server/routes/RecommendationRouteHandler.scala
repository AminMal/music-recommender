package scommender
package server.routes

import service.algebra.RecommendationServiceAlgebra
import utils.ApplicationJsonSupport
import utils.box.BoxSupport

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
                                )(implicit ec: ExecutionContext) extends BoxSupport {

  import RecommendationRouteHandler._

  val route: Route = path("recommend" / IntNumber) { userId =>
    parameter("count".as[Int].withDefault(6)) { count =>
      val result = recommendationService.getRecommendations(userId, count)

      onSuccess(result.toScommenderResponse) { res =>
        complete(status = res.status, res)
      }
    }
  }

}

object RecommendationRouteHandler extends ApplicationJsonSupport