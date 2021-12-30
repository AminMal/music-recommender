package ir.ac.usc
package models.responses


/**
 * Error details returned in failure response
 *
 * @param code    error code (could be resulting http status code)
 * @param message optional message to provide more information about the error
 */
case class ErrorBody(
                      code: Int,
                      message: Option[String]
                    )

object ErrorBody {
  final val InternalServerError = ErrorBody(code = 500, message = Some(ResponseMessage.InternalServerError))
  final val NotImplementedYet = ErrorBody(code = 501, message = Some(ResponseMessage.NotImplementedYet))
}
