package ir.ac.usc
package controllers

import conf.{ALSConfig, ALSDefaultConf}

import akka.actor.{Actor, ActorLogging, Props}


/**
 * This actor takes care of ALS configurations that the application uses in order to make recommendations.
 */
class ConfigManagerActor extends Actor with ActorLogging {
  import Bootstrap.services

  import ConfigManagerActor.Messages._
  import ConfigManagerActor.Response._

  def receive: Receive = receiveWithConf(ALSDefaultConf)

  def receiveWithConf(configurations: ALSConfig): Receive = {
    case UpdateConfig(conf, force) =>
      log.info(s"updating program ALS config with config: $conf")
      context.become(receiveWithConf(conf))
      if (force) {
        services.contextManagerService.updateModel()
      }
      sender() ! ConfigurationsUpdated(conf)

    case GetCurrentConf =>
      sender() ! configurations
  }

}

object ConfigManagerActor {

  /**
   * Generates configuration manager actor Props in order to create new reference of this actor.
   * @return Props of configuration manager actor.
   */
  def props: Props = Props(new ConfigManagerActor)

  /**
   * Messages that this actor accepts.
   */
  object Messages {
    case class UpdateConfig(config: ALSConfig, force: Boolean = false)
    case object GetCurrentConf
  }

  /**
   * Responses that this actor generates.
   */
  object Response {
    case class ConfigurationsUpdated(latestConf: ALSConfig)
  }

}