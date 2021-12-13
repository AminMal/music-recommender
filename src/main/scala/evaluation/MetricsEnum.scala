package ir.ac.usc
package evaluation

object MetricsEnum extends Enumeration {

  type MetricsEnum = Value

  val MSE: MetricsEnum = Value("mse")
  val RMSE: MetricsEnum = Value("rmse")
  val R2: MetricsEnum = Value("r2")
  val MAE: MetricsEnum = Value("mae")
  val VAR: MetricsEnum = Value("var")
  val Shuffled: MetricsEnum = Value("shuffled")
  val PrecisionRecall: MetricsEnum = Value("precision and recall")
  val FMeasure: MetricsEnum = Value("F-Measure")


}
