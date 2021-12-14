package ir.ac.usc
package service

import service.algebra.{
  ApplicationStatusServiceAlgebra,
  ConfigurationManagementServiceAlgebra,
  ContextManagerServiceAlgebra,
  PerformanceEvaluatorServiceAlgebra,
  RecommendationServiceAlgebra
}
import service.impl.{
  ApplicationStatusService,
  ConfigurationManagerService,
  ContextManagerService,
  PerformanceEvaluatorService,
  RecommendationService
}

import akka.actor.ActorSystem
import controllers.{
  ApplicationStatusController,
  ConfigManagerActor,
  ContextManagerActor,
  PerformanceEvaluatorActor,
  RecommenderManagerActor
}


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
      () => system.actorOf(PerformanceEvaluatorActor.props)
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