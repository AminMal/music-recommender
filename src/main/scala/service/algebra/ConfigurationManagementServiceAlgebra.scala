package ir.ac.usc
package service.algebra

import conf.ALSConfig
import controllers.ConfigManagerActor.Response.ConfigurationsUpdated

import scala.concurrent.Future


/**
 * Service representing configuration manager actor
 */
trait ConfigurationManagementServiceAlgebra {

  /**
   * fetch and return latest configuration used in application
   *
   * @return current application configuration wrapped future
   */
  def getLatestConfig: Future[ALSConfig]

  /**
   * update application configuration
   *
   * @param config the config object to be replaces as the latest config
   * @param forced if this parameter is set to true, the ALS model will also be updated right away
   * @return nothing, just a ConfigurationsUpdated message with the given config
   */
  def updateConfig(config: ALSConfig, forced: Boolean): Future[ConfigurationsUpdated]

}
