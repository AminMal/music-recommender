package ir.ac.usc
package service.impl

import controllers.ApplicationStatusController.Messages.HealthCheck
import controllers.ApplicationStatusController.Responses._
import service.algebra.ApplicationStatusServiceAlgebra

import akka.actor.ActorRef
import akka.util.Timeout
import utils.box.BoxF

import scala.concurrent.ExecutionContext


class ApplicationStatusService(statusControllerActor: ActorRef)(
                              implicit timeout: Timeout,
                              ec: ExecutionContext
) extends ApplicationStatusServiceAlgebra {

  override def health(): BoxF[HealthCheckResponse] = {
    statusControllerActor ??[HealthCheckResponse] HealthCheck
  }

}
