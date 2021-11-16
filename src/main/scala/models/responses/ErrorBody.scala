package ir.ac.usc
package models.responses

case class ErrorBody(
                    code: Int,
                    message: Option[String]
                    )
