package ir.ac.usc
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.StatusCode

import scala.util.Try


/**
 * Custom exceptions in this domain extend this trait, which defined methods to be able to
 * convert exceptions to Http results easily
 */
trait ScommenderException extends Throwable {
  def toResponseBody: FailureResponse

  def status: StatusCode
}

object ScommenderException {
  /**
   * Converts any type of throwable to 500 ScommenderException
   *
   * @param throwable the original throwable
   * @return new instance of scommender exception
   */
  def adopt(throwable: Throwable): ScommenderException = throwable match {
    case se: ScommenderException => se
    case other =>
      new ScommenderException {
        override def toResponseBody: FailureResponse =
          FailureResponse(error = ErrorBody(code = 500, message = Try(other.getMessage.take(140)).toOption))

        override def status: StatusCode = 500
      }
  }
}
