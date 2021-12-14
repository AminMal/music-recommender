package ir.ac.usc
package service.algebra

import controllers.ApplicationStatusController.Responses.HealthCheckResponse

import scala.concurrent.Future

trait ApplicationStatusServiceAlgebra {

  def health(): Future[HealthCheckResponse]

}
