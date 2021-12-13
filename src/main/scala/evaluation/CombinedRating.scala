package ir.ac.usc
package evaluation

case class CombinedRating(
                         userId: Int,
                         songId: Int,
                         originalRating: Option[Double],
                         prediction: Double
                         )
