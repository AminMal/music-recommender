package ir.ac.usc
package controllers

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import HttpServer.applicationController
import models.responses.SuccessResponse

import akka.pattern.{ask, pipe}
import controllers.ContextManagerActor.Messages.GetLatestModel

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import spray.json.JsString

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ApplicationStatusController extends Actor {
  import ApplicationStatusController.Messages._
  import ApplicationStatusController.Responses._

  override def receive: Receive = initialReceive
  val contextManagerMessageTimeout: Timeout = Timeout(5, TimeUnit.SECONDS)

  import context.dispatcher
  def initialReceive: Receive = {
    case HealthCheck =>
      val modelOptFuture = (HttpServer.contextManagerActor ?GetLatestModel)(contextManagerMessageTimeout)
        .mapTo[Option[MatrixFactorizationModel]]
      modelOptFuture.map { modelOpt =>
        HealthCheckResponse(matrixModelStatus = modelOpt.isDefined)
      } pipeTo sender()
  }

}

object ApplicationStatusController {
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
    }
}
