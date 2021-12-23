package ir.ac.usc
package models.responses


/**
 * Represents failure http response
 * @param success weather the operation was successful or not
 * @param error error details
 */
case class FailureResponse(
                          success: Boolean = false,
                          error: ErrorBody
                          )