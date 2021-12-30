package scommender
package evaluation

import scala.util.Try


/**
 * an enumeration object holding performance evaluation metrics.
 */
object MetricsEnum extends Enumeration {

  type MetricsEnum = Value

  /* Followings are supported by spark out of the box */
  val MSE: MetricsEnum = Value("mse")
  val R2: MetricsEnum = Value("r2")
  val MAE: MetricsEnum = Value("mae")
  val VAR: MetricsEnum = Value("var")

  /* Implemented */
  val RMSE: MetricsEnum = Value("rmse")
  val Shuffled: MetricsEnum = Value("shuffled")
  val PrecisionRecall: MetricsEnum = Value("precision-and-recall")
  val FMeasure: MetricsEnum = Value("F-Measure")

  def withNameOpt(name: String): Option[MetricsEnum] = Try(super.withName(name)).toOption

}
