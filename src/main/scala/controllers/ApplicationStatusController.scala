package ir.ac.usc
package controllers

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import java.time.LocalDateTime


class ApplicationStatusController extends Actor {
  import ApplicationStatusController.Messages._
  import ApplicationStatusController.Responses._
  import Bootstrap.services
  import context.dispatcher

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case HealthCheck =>
      val modelOptFuture = services.contextManagerService.getLatestModel

      modelOptFuture.map { modelOpt =>
        HealthCheckResponse(matrixModelStatus = modelOpt.isDefined)
      } pipeTo sender()
  }

}

object ApplicationStatusController {

  def props: Props = Props(new ApplicationStatusController)

  object Messages {
    case object HealthCheck
  }
  object Responses {
    case class HealthCheckResponse(
                                  matrixModelStatus: Boolean,
                                  currentTime: String = LocalDateTime.now().toString
                                  )

    case class ModelActivationResponse(
                                      operationStatus: Boolean
                                      )
  }
}
