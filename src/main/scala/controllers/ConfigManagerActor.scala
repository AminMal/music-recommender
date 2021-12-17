package ir.ac.usc
package controllers

import conf.{ALSConfig, ALSDefaultConf}

import akka.actor.{Actor, ActorLogging, Props}


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

  def props: Props = Props(new ConfigManagerActor)

  object Messages {
    case class UpdateConfig(config: ALSConfig, force: Boolean = false)
    case object GetCurrentConf
  }
  object Response {
    case class ConfigurationsUpdated(latestConf: ALSConfig)
  }

}