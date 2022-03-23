package scommender
package service.impl

import controllers.ApplicationStatusController.Messages._
import controllers.ApplicationStatusController.Responses._
import service.algebra.ApplicationStatusServiceAlgebra
import utils.box.BoxF

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future

import models.DiagnosticsReport

import scala.concurrent.ExecutionContext


class ApplicationStatusService(statusControllerActor: ActorRef)(
  implicit timeout: Timeout,
  ec: ExecutionContext
) extends ApplicationStatusServiceAlgebra {

  override def health(): BoxF[HealthCheckResponse] = {
    statusControllerActor ??[HealthCheckResponse] HealthCheck
  }

  def getReports: Future[Set[DiagnosticsReport]] =
    (statusControllerActor ? GetReports).mapTo[Set[DiagnosticsReport]]

  def getReport(session: String): Future[Option[DiagnosticsReport]] =
    (statusControllerActor ? GetReport(session)).mapTo[Option[DiagnosticsReport]]
}
