package scommender
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.StatusCode
import utils.box.Box

import scala.util.control.NonFatal


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
    case NonFatal(other) =>
      new ScommenderException {
        override def toResponseBody: FailureResponse =
          FailureResponse(
            error = ErrorBody(code = 500, message = Box(other.getMessage.take(100)).toOption)
          )

        override def status: StatusCode = 500
      }
  }
}
