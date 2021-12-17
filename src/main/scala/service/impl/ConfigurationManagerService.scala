package ir.ac.usc
package service.impl

import conf.ALSConfig
import controllers.ConfigManagerActor.Messages.{GetCurrentConf, UpdateConfig}
import controllers.ConfigManagerActor.Response.ConfigurationsUpdated
import service.algebra.ConfigurationManagementServiceAlgebra

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.concurrent.Future

class ConfigurationManagerService(configManager: ActorRef) extends ConfigurationManagementServiceAlgebra {

  /* 30 seconds is for test suite not to fail, timeout should be moved to another place */
  implicit val timeout: Timeout = Timeout(30, TimeUnit.SECONDS)

  override def getLatestConfig: Future[ALSConfig] = {
    (configManager ? GetCurrentConf).mapTo[ALSConfig]
  }

  override def updateConfig(config: ALSConfig, forced: Boolean): Future[ConfigurationsUpdated] = {
    (configManager ? UpdateConfig(config, forced)).mapTo[ConfigurationsUpdated]
  }

}
