package ir.ac.usc
package models.responses

case class SuccessResponse[D](
                             success: Boolean = true,
                             data: D
                             )

object SuccessResponse {
  def forBody[T](body: T): SuccessResponse[T] = SuccessResponse(success = true, data = body)

  def forMessage(message: String): SuccessResponse[ResponseMessage] =
    SuccessResponse(success = true, data = ResponseMessage(message))
}
