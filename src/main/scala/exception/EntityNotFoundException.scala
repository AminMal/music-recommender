package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

case class EntityNotFoundException(entity: String, id: Option[String] = None) extends ScommenderException {
  override def toResponseBody: FailureResponse = FailureResponse(
    error = ErrorBody(
      code = 404, message = Some(s"entity: $entity not found${id.map(x => " for identifier " concat x).getOrElse("")}.")
    )
  )

  override def status: StatusCode = StatusCodes.NotFound
}
