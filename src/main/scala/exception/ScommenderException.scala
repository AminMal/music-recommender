package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.StatusCode
import scala.util.Try


trait ScommenderException extends Throwable {
  def toResponseBody: FailureResponse
  def status: StatusCode
}

object ScommenderException {
  def adopt(throwable: Throwable): ScommenderException = new ScommenderException {
    override def toResponseBody: FailureResponse =
      FailureResponse(error = ErrorBody(code = 500, message = Try(throwable.getMessage.take(20)).toOption))

    override def status: StatusCode = 500
  }
}
