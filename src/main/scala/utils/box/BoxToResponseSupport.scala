package scommender
package utils.box

import spray.json.RootJsonWriter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import utils.ApplicationJsonSupport

import akka.Done
import models.responses.{FailureResponse, ResponseMessage, SuccessResponse}

import scala.concurrent.ExecutionContext

trait BoxToResponseSupport extends BoxSupport with ApplicationJsonSupport {

  implicit def unitToResponse(doneBox: BoxF[Done])(implicit ec: ExecutionContext): Route =
    onSuccess(doneBox.toScommenderResponse) {
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

  implicit def completeBoxF[T : RootJsonWriter](value: BoxF[T])(implicit ec: ExecutionContext): Route =
    onSuccess(value.toScommenderResponse) { result =>
      complete(result.status, result)
    }

  implicit def completeBox[T](value: Box[T])(implicit writer: RootJsonWriter[T]): Route =
    value fold (
      fa = scommenderException => complete(scommenderException.status, scommenderException.toResponseBody),
      fb = result => complete(StatusCodes.OK, writer.write(result))
    )

}
