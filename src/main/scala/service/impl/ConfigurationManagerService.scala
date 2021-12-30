package scommender
package service.impl

import conf.ALSConfig
import controllers.ConfigManagerActor.Messages.{GetCurrentConf, UpdateConfig}
import controllers.ConfigManagerActor.Response.ConfigurationsUpdated
import service.algebra.ConfigurationManagementServiceAlgebra

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future

class ConfigurationManagerService(configManager: ActorRef)
                                 (implicit timeout: Timeout) extends ConfigurationManagementServiceAlgebra {

  override def getLatestConfig: Future[ALSConfig] = {
    (configManager ? GetCurrentConf).mapTo[ALSConfig]
  }

  override def updateConfig(config: ALSConfig, forced: Boolean): Future[ConfigurationsUpdated] = {
    (configManager ? UpdateConfig(config, forced)).mapTo[ConfigurationsUpdated]
  }

}
