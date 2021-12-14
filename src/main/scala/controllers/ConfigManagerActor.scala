package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging, Props}
import conf.{ALSConfig, ALSDefaultConf}


class ConfigManagerActor extends Actor with ActorLogging {
  import ConfigManagerActor.Messages._
  import Bootstrap.services

  def receive: Receive = receiveWithConf(ALSDefaultConf)

  def receiveWithConf(configurations: ALSConfig): Receive = {
    case UpdateConfig(conf, force) =>
      log.info(s"updating program ALS config with config: $conf")
      context.become(receiveWithConf(conf))
      if (force) {
        services.contextManagerService.updateModel()
      }

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

}