package ir.ac.usc
package exception
import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

object ModelNotTrainedYetException extends ScommenderException {

  override def toResponseBody: FailureResponse =
    FailureResponse(
      error = ErrorBody(code = 501, message = Some("model not trained yet"))
    )

  override def status: StatusCode = StatusCodes.NotImplemented
}
