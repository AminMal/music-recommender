package ir.ac.usc
package service

import controllers._
import service.algebra._
import service.impl._

import akka.actor.ActorSystem


trait ServiceModule {

  /* Only implementation of actor system is required to get started */
  val system: ActorSystem

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
    )(system.dispatcher)

}

object ServiceModule {
  def forSystem(actorSystem: ActorSystem): ServiceModule = new ServiceModule {
    override val system: ActorSystem = actorSystem
  }
}