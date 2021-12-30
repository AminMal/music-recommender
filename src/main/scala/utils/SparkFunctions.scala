package ir.ac.usc
package utils


/**
 * User defined functions used in program are stored here.
 */
object SparkFunctions {

  val getError: (Double, Double) => Double = (a, b) => {
    val err = a - b
    err * err
  }

  val getState: (Double, Double, Double) => String = (prediction, rate, threshold) => {
    (prediction >= threshold, rate >= threshold) match {
      case (true, true) => "TP" // True Positive
      case (true, false) => "FP" // False positive
      case (false, false) => "TN" // true negative
      case (false, true) => "FN" // False negative
    }
  }

  val fMeasure: (Double, Double) => Double = (precision, recall) => {
    (2 * precision * recall) / (precision + recall)
  }

}
