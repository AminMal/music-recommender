package ir.ac.usc
package server.routes

import controllers.ContextManagerActor.Messages.AddUserRating
import controllers.ContextManagerActor.Responses.{CMOperationResult, OperationFailure, SuccessfulOperation}
import models.responses.{ErrorBody, FailureResponse, ResponseMessage, SuccessResponse}
import models.{SongDTO, User}
import service.algebra.ContextManagerServiceAlgebra

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport

class ApplicationContextRouteHandler(contextManagerService: ContextManagerServiceAlgebra) {

  import ApplicationContextRouteHandler._

  private def completeWriteResult(result: CMOperationResult): Route = result match {
    case SuccessfulOperation =>
      complete(
        status = StatusCodes.OK,
        SuccessResponse.forMessage(
          ResponseMessage.ObjectWriteSuccessful
        )
      )
    case OperationFailure(_) =>
      complete(
        status = StatusCodes.InternalServerError,
        FailureResponse(
          error = ErrorBody.InternalServerError
        )
      )
  }

  val routes: Route = path("rating") {
    post {
      entity(as[AddUserRating]) { ratingRequest =>

        val managerResponse = contextManagerService.addUserRating(ratingRequest)
        onSuccess(managerResponse) (completeWriteResult)

      }
    }
  } ~ path("user") {
    post {
      entity(as[User]) { user =>
        val managerResponse = contextManagerService.addUser(user)

        onSuccess(managerResponse) (completeWriteResult)
      }
    }
  } ~ path("song") {
    post {
      entity(as[SongDTO]) { song =>
        val managerResponse = contextManagerService.addSong(song)

        onSuccess(managerResponse) (completeWriteResult)
      }
    }
  } ~ path("force-update") {
    put {
      contextManagerService.updateModel()
      complete(
        status = StatusCodes.OK,
        SuccessResponse.forMessage("update request sent successfully")
      )
    }
  }

}

object ApplicationContextRouteHandler extends ApplicationJsonSupport