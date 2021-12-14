package ir.ac.usc
package utils

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS

object Common {

  def timeTrack[V](code: => V)(operationName: Option[String] = None, timeUnit: ChronoUnit = SECONDS): V = {
    val start = LocalTime.now()
    val result = code
    val finish = LocalTime.now()
    println(operationName.map(operation => s"Finished $operation, ").getOrElse("") + s"operation took ${timeUnit.between(start, finish)} ${timeUnit.toString}")
    result
  }

}
