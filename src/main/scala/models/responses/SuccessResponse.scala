package ir.ac.usc
package models.responses

case class SuccessResponse[D](
                             success: Boolean = true,
                             data: D
                             )
