package ir.ac.usc
package service.algebra

import conf.ALSConfig
import controllers.ConfigManagerActor.Response.ConfigurationsUpdated

import scala.concurrent.Future

trait ConfigurationManagementServiceAlgebra {

  def getLatestConfig: Future[ALSConfig]
  def updateConfig(config: ALSConfig, forced: Boolean): Future[ConfigurationsUpdated]

}
