package scommender
package exception
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import models.responses.{ErrorBody, FailureResponse}

case class NotImplementedYet(
                              entityKey: String,
                              entityValue: String
                            ) extends ScommenderException {

  override def status: StatusCode = StatusCodes.NotImplemented

  override def toResponseBody: FailureResponse = FailureResponse(
    error = ErrorBody(code = status.intValue(), message = Some(
      s"$entityKey: $entityValue not implemented yet"
    ))
  )
}
