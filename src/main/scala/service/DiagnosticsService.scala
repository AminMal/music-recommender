package scommender
package service

import Bootstrap.appConfig
import conf.ALSConfig
import controllers.ApplicationStatusController
import controllers.ApplicationStatusController.Messages._
import models.ModelPerformanceResult

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

trait DiagnosticsService {

  def initiateSession(model: MatrixFactorizationModel): String

  def updateModelLoadingTimeReport(model: MatrixFactorizationModel, loadTimeInMillis: Long): Unit

  def updateModelTrainingReport(model: MatrixFactorizationModel, trainingTimeMillis: Long, config: ALSConfig): Unit

  def incrementModelServedRequestsForReport(model: MatrixFactorizationModel): Unit

  def updateSessionPerformanceResultsForReport(
                                                model: MatrixFactorizationModel,
                                                performanceResult: ModelPerformanceResult
                                              ): Unit
}

object DiagnosticsService extends DiagnosticsService {

  private final val diagsEnabled = appConfig.getBoolean("scommender.save-diag-history")

  def initiateSession(model: MatrixFactorizationModel): String = {
    if (diagsEnabled) {
      ApplicationStatusController.select ! InitiateDiagnostics(model.hashCode().toString)
    }
    model.hashCode().toString
  }

  def updateModelLoadingTimeReport(model: MatrixFactorizationModel, loadTimeInMillis: Long): Unit = {
    val session = initiateSession(model)
    if (diagsEnabled) {
      ApplicationStatusController.select ! UpdateLoadingTime(session, loadTimeInMillis)
    }
  }

  def updateModelTrainingReport(model: MatrixFactorizationModel, trainingTimeMillis: Long, config: ALSConfig): Unit = {
    val session = initiateSession(model)
    if (diagsEnabled) {
      ApplicationStatusController.select ! UpdateCreationTime(session, trainingTimeMillis, config)
    }
  }

  def incrementModelServedRequestsForReport(model: MatrixFactorizationModel): Unit =
    if (diagsEnabled) {
      ApplicationStatusController.select ! IncrementModelServedRequests(model.hashCode().toString)
    }

  def updateSessionPerformanceResultsForReport(
                                                model: MatrixFactorizationModel,
                                                performanceResult: ModelPerformanceResult
                                              ): Unit = if (diagsEnabled) {
    ApplicationStatusController.select ! UpdateSessionPerformance(
      model.hashCode().toString, performanceResult
    )
  }


}
