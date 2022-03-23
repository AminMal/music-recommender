package scommender
package exception

import models.responses.{ErrorBody, FailureResponse}

import akka.http.scaladsl.model.StatusCode
import utils.box.Box

import scala.reflect.ClassTag
import scala.util.control.NonFatal


/**
 * Custom exceptions in this domain extend this trait, which defined methods to be able to
 * convert exceptions to Http results easily
 */
trait ScommenderException extends Throwable {
  def toResponseBody: FailureResponse

  def status: StatusCode

  def isAdopted: Boolean = false

  def getOriginal: Option[Throwable] = None

  def isOfType[T : ClassTag]: Boolean = false
}

final class AdoptedScommenderException(cause: Throwable, statusCode: Int = 500) extends ScommenderException {
  override def toResponseBody: FailureResponse =
    FailureResponse(
      error = ErrorBody(code = statusCode, message = Box(cause.getMessage.take(100)).toOption)
    )

  override def status: StatusCode = StatusCode.int2StatusCode(statusCode)

  override def isAdopted: Boolean = true

  override def getOriginal: Option[Throwable] = Some(cause)

  override def isOfType[T: ClassTag]: Boolean = cause.isInstanceOf[T]
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
      new AdoptedScommenderException(other)
  }

  def adopt(throwable: Throwable, status: Int): ScommenderException = throwable match {
    case se: ScommenderException => se
    case NonFatal(other) =>
      new AdoptedScommenderException(other, status)
  }
}
