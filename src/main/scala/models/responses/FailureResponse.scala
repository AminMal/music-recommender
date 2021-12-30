package scommender
package models.responses

import akka.http.scaladsl.model.StatusCode


/**
 * Represents failure http response
 *
 * @param success weather the operation was successful or not
 * @param error   error details
 */
case class FailureResponse(
                            success: Boolean = false,
                            error: ErrorBody
                          ) extends ScommenderResponse[Nothing] {
  override def status: StatusCode = StatusCode.int2StatusCode(error.code)
}