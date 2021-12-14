package ir.ac.usc
package service.impl

import service.algebra.ApplicationStatusServiceAlgebra

import akka.actor.ActorRef
import controllers.ApplicationStatusController.Responses._
import controllers.ApplicationStatusController.Messages.HealthCheck

import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.Future


class ApplicationStatusService(statusControllerActor: ActorRef) extends ApplicationStatusServiceAlgebra {

  implicit val timeout: Timeout = Timeout(2, TimeUnit.SECONDS)
  override def health(): Future[HealthCheckResponse] = {
    (statusControllerActor ? HealthCheck).mapTo[HealthCheckResponse]
  }

}
