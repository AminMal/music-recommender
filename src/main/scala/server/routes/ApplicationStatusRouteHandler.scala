package ir.ac.usc
package server.routes

import models.responses.SuccessResponse
import service.algebra.ApplicationStatusServiceAlgebra

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport
import utils.box.BoxSupport

import scala.concurrent.ExecutionContext


/**
 * This class handles http requests for application status actor
 * @param applicationService application status service
 */
class ApplicationStatusRouteHandler(applicationService: ApplicationStatusServiceAlgebra)(
                                   implicit ec: ExecutionContext
) extends BoxSupport {

  import ApplicationStatusRouteHandler._

  val route: Route = path("health") {
    get {
      val result = applicationService.health()
      onSuccess(result.toScommenderResponse) { res =>
        complete(res.status, res)
      }
    }
  }

}

object ApplicationStatusRouteHandler extends ApplicationJsonSupport