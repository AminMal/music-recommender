package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.{StatusCode, StatusCodes}


/**
 * Singleton object, indicating that there is no model currently available to make recommendations.
 */
object ModelNotTrainedYetException extends ScommenderException {

  override def toResponseBody: FailureResponse =
    FailureResponse(
      error = ErrorBody(code = 501, message = Some("model not trained yet"))
    )

  override def status: StatusCode = StatusCodes.NotImplemented
}
