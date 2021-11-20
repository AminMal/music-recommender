package ir.ac.usc
package controllers

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import HttpServer.applicationController
import models.responses.SuccessResponse

import spray.json.JsString

import java.time.LocalDateTime

class ApplicationStatusController extends Actor {
  import ApplicationStatusController.Messages._
  import ApplicationStatusController.Responses._

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case ModelActivated =>
      context.become(handler)
      sender() ! ModelActivationResponse(true)
    case HealthCheck =>
      sender() ! HealthCheckResponse(matrixModelStatus = false)
    case _ => sender() ! "Training model"
  }

  def handler: Receive = {
    case HealthCheck =>
      sender() ! HealthCheckResponse(matrixModelStatus = true)
    case ping => sender() ! "pong"
    case _ => sender() ! "I don't recognize this message"
  }

}

object ApplicationStatusController {
  object Messages {
    case object HealthCheck
    case object ModelActivated
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

  import Messages._
  import Responses._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import Bootstrap.JsonImplicits._

  def routes(implicit timeout: Timeout) =
    path("health") {
      get {
        val result = (applicationController ? HealthCheck).mapTo[HealthCheckResponse]
        onSuccess(result) { res =>
          complete(status = StatusCodes.OK, SuccessResponse(data = res))
        }
      }
    } ~ path("ping") {
      get {
        val result = (applicationController ? "ping").mapTo[String]
        onSuccess(result) { res =>
          complete(status = StatusCodes.OK, JsString(res))
        }
      }
    }
}
