package ir.ac.usc
package models.responses

/**
 * A case class holding message information to be sent in response body
 *
 * @param message message to be sent as response body
 */
case class ResponseMessage(
                            message: String
                          )

object ResponseMessage {
  final val ObjectWriteSuccessful = "data appended successfully"
  final val InternalServerError = "internal server error"
  final val NotImplementedYet = "Not implemented yet"
}