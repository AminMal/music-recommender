package scommender
package server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import controllers.ApplicationStatusController.Responses.HealthCheckResponse
import models.responses.SuccessResponse


class ApplicationStatusRouteSpec extends RouteProvider("application-route-spec") {

  val route: Route = provider.routes

  "application route handler service" should {
    "return successful health check response" in {
      Get("/health") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[SuccessResponse[HealthCheckResponse]].success shouldBe true
      }
    }
  }

}
