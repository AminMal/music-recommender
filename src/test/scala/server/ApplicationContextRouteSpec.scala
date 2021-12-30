package scommender
package server

import akka.http.scaladsl.model.StatusCodes
import models.responses.{ResponseMessage, SuccessResponse}
import controllers.ContextManagerActor.Messages.AddUserRating
import models.{SongDTO, User}


class ApplicationContextRouteSpec extends RouteProvider("application-context-route-spec") {

  import provider._

  val addUserRating: AddUserRating = AddUserRating(1, 2, 3.0)
  val user: User = User(1, None, "none")
  val song: SongDTO = SongDTO(0L, "good song", 9L, "23|34", "artist", 2.0)

  "application context route service" should {
    "add rating to df" in {
      Post("/rating", content = addUserRating) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessResponse[ResponseMessage]].data.message shouldEqual ResponseMessage.ObjectWriteSuccessful
      }
    }

    "add user" in {
      Post("/user", content = user) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessResponse[ResponseMessage]].data.message shouldEqual ResponseMessage.ObjectWriteSuccessful
      }
    }

    "add song" in {
      Post("/song", content = song) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessResponse[ResponseMessage]].data.message shouldEqual ResponseMessage.ObjectWriteSuccessful
      }
    }

    "trigger force update application context and return successful response" in {
      Put("/force-update") ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessResponse[ResponseMessage]].data.message shouldEqual "update request sent successfully"
      }
    }
  }

}
