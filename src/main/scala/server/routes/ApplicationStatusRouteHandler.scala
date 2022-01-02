package scommender
package server.routes

import service.algebra.ApplicationStatusServiceAlgebra
import utils.ApplicationJsonSupport
import utils.box.{BoxSupport, BoxToResponseSupport}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext


/**
 * This class handles http requests for application status actor
 *
 * @param applicationService application status service
 */
class ApplicationStatusRouteHandler(applicationService: ApplicationStatusServiceAlgebra)(
  implicit ec: ExecutionContext
) extends BoxToResponseSupport {

  val route: Route = path("health") {
    get {
      applicationService.health()
    }
  }

}