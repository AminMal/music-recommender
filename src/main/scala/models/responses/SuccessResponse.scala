package ir.ac.usc
package models.responses


/**
 * Represents successful http result
 * @param success weather the operation was successful or not
 * @param data actual response data
 * @tparam D type of data parameter
 */
case class SuccessResponse[D](
                             success: Boolean = true,
                             data: D
                             )

object SuccessResponse {
  /**
   * Creates a successful response for a string message
   * @param message string message to return as body
   * @return an object of successful response for the message
   */
  def forMessage(message: String): SuccessResponse[ResponseMessage] =
    SuccessResponse(data = ResponseMessage(message))
}
