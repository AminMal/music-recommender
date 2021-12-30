package ir.ac.usc
package utils

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS

/**
 * Common utility object for all the packages.
 */
object Common {

  /**
   * evaluate the given code, printing the time it took to evaluate the code
   * @param code the input code block
   * @param operationName operation name for better printing
   * @param timeUnit time unit for operation
   * @tparam V Type of the code expression
   * @return value of running the code
   */
  def timeTrack[V](code: => V)(operationName: Option[String] = None, timeUnit: ChronoUnit = SECONDS): V = {
    val start = LocalTime.now()
    val result = code
    val finish = LocalTime.now()
    println(operationName.map(operation => s"Finished $operation, ").getOrElse("") + s"operation took ${timeUnit.between(start, finish)} ${timeUnit.toString}")
    result
  }

}
