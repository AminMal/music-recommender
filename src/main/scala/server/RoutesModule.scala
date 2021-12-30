package scommender
package server

import server.routes._
import service.ServiceModule

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


/**
 * Given service module, this class can handle http requests with fault tolerance
 *
 * @param services an instance of service module
 */
class RoutesModule(services: ServiceModule)(implicit routeHandlerSystem: ActorSystem) {

  import routeHandlerSystem.dispatcher

  val applicationRouteHandler = new ApplicationStatusRouteHandler(
    services.applicationStatusService
  )

  val contextRouteHandler = new ApplicationContextRouteHandler(
    services.contextManagerService
  )

  val configurationsRouteHandler = new ConfigurationsRouteHandler(
    services.configurationManagementService
  )

  val recommendationsRouteHandler = new RecommendationRouteHandler(
    services.recommendationManagerService
  )

  val performanceRouteHandler = new PerformanceEvaluatorRouteHandler(
    services.performanceEvaluatorService
  )

  val routes: Route = {
    handleExceptions(ApplicationExceptionHandler.handler) {
      applicationRouteHandler.route ~
        contextRouteHandler.routes ~
        configurationsRouteHandler.routes ~
        recommendationsRouteHandler.route ~
        performanceRouteHandler.route
    }
  }

}