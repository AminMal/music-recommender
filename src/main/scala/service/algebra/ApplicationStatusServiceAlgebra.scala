package scommender
package service.algebra

import controllers.ApplicationStatusController.Responses.HealthCheckResponse
import utils.box.{BoxF, BoxSupport}

import akka.Done
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import models.DiagnosticsReport
import scala.concurrent.Future


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

  def getReports: Future[Set[DiagnosticsReport]]

  def getReport(session: String): Future[Option[DiagnosticsReport]]

}
