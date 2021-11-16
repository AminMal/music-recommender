package ir.ac.usc

import controllers.{ApplicationStatusController, RecommendationController}

import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import java.util.concurrent.TimeUnit

object HttpServer {

  implicit val timeout: Timeout = Timeout(60, TimeUnit.SECONDS)

  private val applicationHandlerRoutes = ApplicationStatusController.routes
  private val recommenerRoutes = RecommendationController.routes

  val route: Route =
    applicationHandlerRoutes ~
      recommenerRoutes

}
