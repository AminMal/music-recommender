package ir.ac.usc
package server.routes

import conf.ALSConfig
import models.responses.SuccessResponse
import service.algebra.ConfigurationManagementServiceAlgebra
import utils.ApplicationJsonSupport

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class ConfigurationsRouteHandler(configService: ConfigurationManagementServiceAlgebra) {

  import ConfigurationsRouteHandler._

  val routes: Route = pathPrefix("conf") {
    (path("update") & put) {
      entity(as[ALSConfig]) { newConfig =>
        parameter("force".as[Boolean].optional) { forced =>
          configService.updateConfig(newConfig, forced.getOrElse(false))

          complete(
            status = StatusCodes.OK,
            SuccessResponse.forMessage("configurations updated successfully!")
          )
        }
      }
    }
  }

}

object ConfigurationsRouteHandler extends ApplicationJsonSupport
