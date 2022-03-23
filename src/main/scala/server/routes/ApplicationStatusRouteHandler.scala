package scommender
package server.routes

import service.algebra.ApplicationStatusServiceAlgebra
import utils.box.BoxToResponseSupport

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import exception.EntityNotFoundException
import models.responses.SuccessResponse
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
  } ~ path("diagnostics" / Segment) { session =>
    get {
      val reportQueryResult = applicationService.getReport(session)
      onSuccess(reportQueryResult) { reportOpt =>
        val report = reportOpt
          .getOrElse(
            throw EntityNotFoundException(
              "report", Some(session)
            )
          )
        complete(SuccessResponse(data = report))
      }
    }
  } ~ path("diagnostics") {
    get {
      val reportsFuture = applicationService.getReports
      onSuccess(reportsFuture) { reports =>
        complete(SuccessResponse(data = reports.toSeq))
      }
    }
  }

}