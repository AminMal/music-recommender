package scommender
package controllers

import utils.box.BoxSupport

import akka.actor.{Actor, Props}

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

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case HealthCheck =>
      val modelBoxed = services.contextManagerService.getLatestModel

      modelBoxed.map { modelOpt =>
        HealthCheckResponse(matrixModelStatus = modelOpt.isDefined)
      } pipeTo sender()
  }

}

object ApplicationStatusController {

  /**
   * Generates application status actor Props in order to create new reference of this actor.
   *
   * @return Props for the actor.
   */
  def props: Props = Props(new ApplicationStatusController)

  /**
   * Messages that this actor handles.
   */
  object Messages {
    case object HealthCheck
  }

  /**
   * Responses that this actor generates.
   */
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
