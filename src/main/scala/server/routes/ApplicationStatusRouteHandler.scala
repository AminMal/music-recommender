package ir.ac.usc
package server.routes

import models.responses.SuccessResponse
import service.algebra.ApplicationStatusServiceAlgebra

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport

class ApplicationStatusRouteHandler(applicationService: ApplicationStatusServiceAlgebra) {

  import ApplicationStatusRouteHandler._

  val route: Route = path("health") {
    get {
      val result = applicationService.health()
      onSuccess(result) { res =>
        complete(status = StatusCodes.OK, SuccessResponse(data = res))
      }
    }
  }

}

object ApplicationStatusRouteHandler extends ApplicationJsonSupport