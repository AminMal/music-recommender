package ir.ac.usc
package server

import service.ServiceModule

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import server.routes.{ApplicationContextRouteHandler, ApplicationStatusRouteHandler, ConfigurationsRouteHandler, PerformanceEvaluatorRouteHandler, RecommendationRouteHandler}


class RoutesModule(services: ServiceModule) {

  lazy val applicationRouteHandler = new ApplicationStatusRouteHandler(
    services.applicationStatusService
  )

  lazy val contextRouteHandler = new ApplicationContextRouteHandler(
    services.contextManagerService
  )

  lazy val configurationsRouteHandler = new ConfigurationsRouteHandler(
    services.configurationManagementService
  )

  lazy val recommendationsRouteHandler = new RecommendationRouteHandler(
    services.recommendationManagerService
  )

  lazy val performanceRouteHandler = new PerformanceEvaluatorRouteHandler(
    services.performanceEvaluatorService
  )

  lazy val routes: Route =
    applicationRouteHandler.route ~
      contextRouteHandler.routes ~
      configurationsRouteHandler.routes ~
      recommendationsRouteHandler.route ~
      performanceRouteHandler.route

}