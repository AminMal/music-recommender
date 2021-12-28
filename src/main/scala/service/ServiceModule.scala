package ir.ac.usc
package service

import controllers._
import service.algebra._
import service.impl._

import akka.actor.ActorSystem
import controllers.ApplicationStatusController.Responses.HealthCheckResponse

import akka.util.Timeout
import scala.concurrent.{ExecutionContext, Future}


/**
 * This trait holds all the services required inside.
 */
sealed class ServiceModule(val system: ActorSystem)(implicit timeout: Timeout) {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val applicationStatusService: ApplicationStatusServiceAlgebra =
    new ApplicationStatusService(
      system.actorOf(ApplicationStatusController.props)
    )


  lazy val configurationManagementService: ConfigurationManagementServiceAlgebra =
    new ConfigurationManagerService(
      system.actorOf(ConfigManagerActor.props)
    )

  lazy val contextManagerService: ContextManagerServiceAlgebra =
    new ContextManagerService(
      system.actorOf(ContextManagerActor.props)
    )

  lazy val performanceEvaluatorService: PerformanceEvaluatorServiceAlgebra =
    new PerformanceEvaluatorService(
      () => system.actorOf(PerformanceEvaluatorActor.props),
      contextManagerService
    )

  lazy val recommendationManagerService: RecommendationServiceAlgebra =
    new RecommendationService(
      system.actorOf(RecommenderManagerActor.props)
    )(system.dispatcher, timeout)
}

object ServiceModule {

  /**
   * Creates an instance of service module for given actor system
   * @param actorSystem service actor system
   * @param to timeout for messages
   * @return a new instance of service module
   */
  def forSystem(actorSystem: ActorSystem)(implicit to: Timeout): ServiceModule =
    new ServiceModule(actorSystem)
}