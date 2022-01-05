package scommender
package server

import server.routes._
import service.ServiceModule

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.DataFrameProvider


/**
 * Given service module, this class can handle http requests with fault tolerance
 *
 * @param services an instance of service module
 */
class RoutesModule(
                    services: ServiceModule,
                    dataframeProvider: DataFrameProvider
                  )(implicit routeHandlerSystem: ActorSystem) {

  import routeHandlerSystem.dispatcher

  private val applicationRouteHandler = new ApplicationStatusRouteHandler(
    services.applicationStatusService
  )

  private val contextRouteHandler = new ApplicationContextRouteHandler(
    services.contextManagerService
  )

  private val configurationsRouteHandler = new ConfigurationsRouteHandler(
    services.configurationManagementService
  )

  private val recommendationsRouteHandler = new RecommendationRouteHandler(
    services.recommendationManagerService
  )

  private val performanceRouteHandler = new PerformanceEvaluatorRouteHandler(
    services.performanceEvaluatorService,
    dataframeProvider
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