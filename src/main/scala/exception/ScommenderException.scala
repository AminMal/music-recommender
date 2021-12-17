package ir.ac.usc
package exception

import models.responses.FailureResponse

import akka.http.scaladsl.model.StatusCode

trait ScommenderException extends Throwable {
  def toResponseBody: FailureResponse
  def status: StatusCode
}
