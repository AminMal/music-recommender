package ir.ac.usc
package service.impl

import service.algebra.ConfigurationManagementServiceAlgebra

import akka.actor.ActorRef
import conf.ALSConfig

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import controllers.ConfigManagerActor.Messages.{GetCurrentConf, UpdateConfig}

import java.util.concurrent.TimeUnit

class ConfigurationManagerService(configManager: ActorRef) extends ConfigurationManagementServiceAlgebra {

  implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

  override def getLatestConfig: Future[ALSConfig] = {
    (configManager ? GetCurrentConf).mapTo[ALSConfig]
  }

  override def updateConfig(config: ALSConfig, forced: Boolean): Unit = {
    configManager ! UpdateConfig(config, forced)
  }

}
