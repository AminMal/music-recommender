package ir.ac.usc
package models.responses

case class FailureResponse(
                          success: Boolean = false,
                          error: ErrorBody
                          )