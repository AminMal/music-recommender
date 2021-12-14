package ir.ac.usc
package service.algebra

import conf.ALSConfig

import scala.concurrent.Future

trait ConfigurationManagementServiceAlgebra {

  def getLatestConfig: Future[ALSConfig]
  def updateConfig(config: ALSConfig, forced: Boolean)

}
