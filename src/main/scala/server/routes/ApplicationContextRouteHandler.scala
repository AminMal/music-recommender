package scommender
package server.routes

import controllers.ContextManagerActor.Messages.AddUserRating
import models.responses.{FailureResponse, ResponseMessage, ScommenderResponse, SuccessResponse}
import models.{SongDTO, User}
import service.algebra.ContextManagerServiceAlgebra
import utils.ApplicationJsonSupport
import utils.box.{BoxSupport, BoxToResponseSupport}

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
                                    )(implicit ec: ExecutionContext) extends BoxToResponseSupport {

  val routes: Route = path("rating") {
    post {
      entity(as[AddUserRating]) { ratingRequest =>

        contextManagerService.addUserRating(ratingRequest)

      }
    }
  } ~ path("user") {
    post {
      entity(as[User]) { user =>

        contextManagerService.addUser(user)

      }
    }
  } ~ path("song") {
    post {
      entity(as[SongDTO]) { song =>

        contextManagerService.addSong(song)

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