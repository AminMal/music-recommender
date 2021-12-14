package ir.ac.usc
package models.responses

case class SuccessResponse[D](
                             success: Boolean = true,
                             data: D
                             )

object SuccessResponse {
  def forMessage(message: String): SuccessResponse[ResponseMessage] =
    SuccessResponse(data = ResponseMessage(message))
}
