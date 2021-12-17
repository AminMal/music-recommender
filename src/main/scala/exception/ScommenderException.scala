package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.StatusCode

trait ScommenderException extends Throwable {
  def toResponseBody: FailureResponse
  def status: StatusCode
}

object ScommenderException {
  def adopt(throwable: Throwable): ScommenderException = new ScommenderException {
    override def toResponseBody: FailureResponse =
      FailureResponse(error = ErrorBody(code = 500, message = Some(throwable.getMessage.take(20))))

    override def status: StatusCode = 500
  }
}
