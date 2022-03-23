package scommender
package controllers

import utils.box.BoxSupport

import akka.actor.{Actor, ActorSelection, Props}
import conf.ALSConfig
import models.{DiagnosticsReport, ModelPerformanceResult}
import Bootstrap.actorSystem

import java.time.LocalDateTime

/**
 * This actor (controller) takes the responsibility of handling the general health of the application,
 * meaning the live-ness, matrix model status and current local time.
 * Only one reference of this actor is created and available.
 */
class ApplicationStatusController extends Actor with BoxSupport {

  import Bootstrap.services

  import ApplicationStatusController.Messages._
  import ApplicationStatusController.Responses._
  import context.dispatcher

  private def getReceive(history: Set[DiagnosticsReport]): Receive =
    healthCheckHandler orElse receiveWithDiagnostics(history)

  override def receive: Receive = getReceive(Set.empty[DiagnosticsReport])

  def healthCheckHandler: Receive = {
    case HealthCheck =>
      val modelBoxed = services.contextManagerService.getLatestModel

      modelBoxed.map { modelOpt =>
        HealthCheckResponse(matrixModelStatus = modelOpt.isDefined)
      } pipeTo sender()
  }

  private def updateHistoryAndSwitchContext(session: String, history: Set[DiagnosticsReport])(ifFoundReport: DiagnosticsReport => DiagnosticsReport): Unit = {
    val reportOpt = history.find(_.session == session)
    reportOpt.map(ifFoundReport) foreach { updatedReport =>
      context become getReceive(
        history.filterNot(_.session == session) + updatedReport
      )
    }
  }

  def receiveWithDiagnostics(history: Set[DiagnosticsReport]): Receive = {
    case InitiateDiagnostics(session) =>
      context become getReceive(history + DiagnosticsReport(
        session = session, registeredAt = LocalDateTime.now, updatedAt = Some(LocalDateTime.now)
      ))

    case UpdateLoadingTime(session, matrixLoadTime) =>
      updateHistoryAndSwitchContext(session, history)(
        ifFoundReport = oldReport => oldReport.copy(
          loadingDurationMillis = Some(matrixLoadTime), updatedAt = Some(LocalDateTime.now)
        )
      )

    case UpdateCreationTime(session, matrixCreationTime, config) =>
      updateHistoryAndSwitchContext(session, history)(
        ifFoundReport = oldReport => oldReport.copy(
          updatedAt = Some(LocalDateTime.now),
          trainingDurationMillis = Some(matrixCreationTime),
          config = Some(config)
        )
      )

    case IncrementModelServedRequests(session) =>
      updateHistoryAndSwitchContext(session, history)(
        ifFoundReport = oldReport => oldReport.copy(
          updatedAt = Some(LocalDateTime.now),
          servedRecommendationRequests = oldReport.servedRecommendationRequests + 1
        )
      )

    case UpdateSessionConfig(session, config) =>
      updateHistoryAndSwitchContext(session, history)(
        ifFoundReport = _.copy(
          updatedAt = Some(LocalDateTime.now),
          config = Some(config)
        )
      )

    case UpdateSessionPerformance(session, performanceResult) =>
      updateHistoryAndSwitchContext(session, history)(
        ifFoundReport = oldReport => oldReport.copy(
          performance = Some(performanceResult)
        )
      )

    case GetReports =>
      sender ! history

    case GetReport(session) =>
      sender ! history.find(_.session == session)

  }

}

object ApplicationStatusController {

  /**
   * Generates application status actor Props in order to create new reference of this actor.
   *
   * @return Props for the actor.
   */
  def props: Props = Props(new ApplicationStatusController)

  val name = "status-controller"

  val path: String = s"/user/$name"

  def select: ActorSelection = actorSystem.actorSelection(path)

  /**
   * Messages that this actor handles.
   */
  object Messages {
    case object HealthCheck
    case class InitiateDiagnostics(session: String)
    case class UpdateLoadingTime(session: String, loadingTime: Long)
    case class UpdateCreationTime(session: String, creationTime: Long, config: ALSConfig)
    case class IncrementModelServedRequests(session: String)
    case class UpdateSessionConfig(session: String, config: ALSConfig)
    case class UpdateSessionPerformance(session: String, performanceResult: ModelPerformanceResult)

    case object GetReports
    case class GetReport(session: String)
  }

  /**
   * Responses that this actor generates.
   */
  object Responses {
    case class HealthCheckResponse(
                                    matrixModelStatus: Boolean,
                                    currentTime: LocalDateTime = LocalDateTime.now()
                                  )

    case class ModelActivationResponse(
                                        operationStatus: Boolean
                                      )
  }
}
