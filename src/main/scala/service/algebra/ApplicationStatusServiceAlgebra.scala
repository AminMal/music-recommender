package scommender
package service.algebra

import controllers.ApplicationStatusController.Responses.HealthCheckResponse
import utils.box.{BoxF, BoxSupport}


/**
 * Service representing application status actor features
 */
trait ApplicationStatusServiceAlgebra extends BoxSupport {

  /**
   * Application health check api
   *
   * @return health check response wrapped in future
   */
  def health(): BoxF[HealthCheckResponse]

}
