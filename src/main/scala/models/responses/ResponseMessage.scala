package ir.ac.usc
package models.responses

case class ResponseMessage(
                          message: String
                          )

object ResponseMessage {
  final val ObjectWriteSuccessful = "data appended successfully"
  final val InternalServerError = "internal server error"
}