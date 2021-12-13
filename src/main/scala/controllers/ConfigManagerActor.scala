package ir.ac.usc
package controllers

import akka.actor.{Actor, ActorLogging}
import conf.{ALSConfig, ALSDefaultConf}

import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import controllers.ContextManagerActor.Messages.UpdateModel
import models.responses.SuccessResponse

class ConfigManagerActor extends Actor with ActorLogging {
  import ConfigManagerActor.Messages._
  import HttpServer.contextManagerActor

  def receive: Receive = receiveWithConf(ALSDefaultConf)

  def receiveWithConf(configurations: ALSConfig): Receive = {
    case UpdateConfig(conf, force) =>
      log.info(s"updating program ALS config with config: $conf")
      context.become(receiveWithConf(conf))
      if (force) {
        contextManagerActor ! UpdateModel
      }

    case GetCurrentConf =>
      sender() ! configurations
  }

}

object ConfigManagerActor {
  object Messages {
    case class UpdateConfig(config: ALSConfig, force: Boolean = false)
    case object GetCurrentConf
  }

  import Messages._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import Bootstrap.JsonImplicits._
  import HttpServer.configManagerActor

  def routes(implicit timeout: Timeout): Route = pathPrefix("conf") {
    path("update") {
      put {
        entity(as[ALSConfig]) { newConfig =>
          parameter("force".as[Boolean].optional) { forced =>
            configManagerActor ! UpdateConfig(newConfig, forced.getOrElse(false))

            complete(
              status = StatusCodes.OK,
              SuccessResponse.forMessage("configurations updated successfully!")
            )
          }
        }
      }
    }
  }
}