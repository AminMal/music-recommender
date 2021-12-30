package ir.ac.usc
package server.routes

import controllers.ContextManagerActor.Messages.AddUserRating
import models.responses.{FailureResponse, ResponseMessage, ScommenderResponse, SuccessResponse}
import models.{SongDTO, User}
import service.algebra.ContextManagerServiceAlgebra
import utils.ApplicationJsonSupport
import utils.box.BoxSupport

import akka.Done
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext


/**
 * This class handles http requests for context manager actor
 *
 * @param contextManagerService context manager service
 */
class ApplicationContextRouteHandler(
                                      contextManagerService: ContextManagerServiceAlgebra
                                    )(implicit ec: ExecutionContext) extends BoxSupport with ApplicationJsonSupport {

  private def completeScommenderResult(result: ScommenderResponse[Done]): Route = result match {
    case failed@FailureResponse(_, _) =>
      complete(failed.status, failed)
    case success@SuccessResponse(_, _) =>
      complete(
        status = success.status,
        SuccessResponse.forMessage(
          ResponseMessage.ObjectWriteSuccessful
        )
      )
  }

  val routes: Route = path("rating") {
    post {
      entity(as[AddUserRating]) { ratingRequest =>

        val managerResponse = contextManagerService.addUserRating(ratingRequest)
        onSuccess(managerResponse.toScommenderResponse)(completeScommenderResult)

      }
    }
  } ~ path("user") {
    post {
      entity(as[User]) { user =>
        val managerResponse = contextManagerService.addUser(user)

        onSuccess(managerResponse.toScommenderResponse)(completeScommenderResult)
      }
    }
  } ~ path("song") {
    post {
      entity(as[SongDTO]) { song =>
        val managerResponse = contextManagerService.addSong(song)

        onSuccess(managerResponse.toScommenderResponse)(completeScommenderResult)
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