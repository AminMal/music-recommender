package ir.ac.usc
package models.responses

case class ErrorBody(
                    code: Int,
                    message: Option[String]
                    )

object ErrorBody {
  final val InternalServerError = ErrorBody(code = 500, message = Some(ResponseMessage.InternalServerError))
  final val NotImplementedYet   = ErrorBody(code = 501, message = Some(ResponseMessage.NotImplementedYet))
}
