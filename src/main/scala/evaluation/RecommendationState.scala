package ir.ac.usc
package evaluation

case class RecommendationState(
                              truePositives: Long,
                              falsePositives: Long,
                              trueNegatives: Long,
                              falseNegatives: Long
                              )

object RecommendationState {
  val dfColNames: Seq[String] = Seq("TP", "FP", "TN", "FN")
}