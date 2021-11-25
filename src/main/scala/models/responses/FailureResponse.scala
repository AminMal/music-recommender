package ir.ac.usc
package models.responses

case class FailureResponse(
                          success: Boolean = false,
                          error: ErrorBody
                          )

object FailureResponse {
  def withError(error: ErrorBody): FailureResponse =
    FailureResponse(success = false, error = error)
}
