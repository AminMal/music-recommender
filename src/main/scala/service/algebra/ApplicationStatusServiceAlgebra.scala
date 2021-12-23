package ir.ac.usc
package service.algebra

import controllers.ApplicationStatusController.Responses.HealthCheckResponse

import scala.concurrent.Future


/**
 * Service representing application status actor features
 */
trait ApplicationStatusServiceAlgebra {

  /**
   * Application health check api
   * @return health check response wrapped in future
   */
  def health(): Future[HealthCheckResponse]

}
