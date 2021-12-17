package ir.ac.usc
package service.impl

import controllers.ApplicationStatusController.Messages.HealthCheck
import controllers.ApplicationStatusController.Responses._
import service.algebra.ApplicationStatusServiceAlgebra

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.concurrent.Future


class ApplicationStatusService(statusControllerActor: ActorRef) extends ApplicationStatusServiceAlgebra {

  implicit val timeout: Timeout = Timeout(30, TimeUnit.SECONDS)
  override def health(): Future[HealthCheckResponse] = {
    (statusControllerActor ? HealthCheck).mapTo[HealthCheckResponse]
  }

}
