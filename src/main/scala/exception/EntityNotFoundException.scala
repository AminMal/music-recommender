package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.{StatusCode, StatusCodes}


/**
 * This class represents that some entity (optionally with an id) could not be found
 *
 * @param entity the thing that could not be found
 * @param id     optional id for the entity
 */
case class EntityNotFoundException(entity: String, id: Option[String] = None) extends ScommenderException {
  override def toResponseBody: FailureResponse = FailureResponse(
    error = ErrorBody(
      code = 404, message = Some(s"entity: $entity not found${id.map(x => " for identifier " concat x).getOrElse("")}.")
    )
  )

  override def status: StatusCode = StatusCodes.NotFound
}
