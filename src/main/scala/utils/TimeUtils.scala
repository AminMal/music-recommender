package scommender
package utils

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.{MILLIS, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Common utility object for all the packages.
 */
object TimeUtils {

  private def now(): LocalTime = LocalTime.now()

  private def logJob(name: String, timeUnit: ChronoUnit, length: Long): Unit =
    println(s"Finished $name, operation took $length ${timeUnit.toString}")

  /**
   * evaluate the given code, printing the time it took to evaluate the code
   *
   * {{{
   *   def readFile(filePath: String): Seq[String] = {
   *     openFile(filePath).readLines
   *   }
   *
   *   val fileContents = timeTrack(operationName = "reading file contents", ChronoUnit.MILLIS) {
   *     readFile(filePath)
   *   }
   *
   *   Console> Finished reading file contents, operation took 129 Millis.
   * }}}
   *
   * @param code          the input code block
   * @param operationName operation name for better printing
   * @param timeUnit      time unit for operation
   * @tparam V Type of the code expression
   * @return value of running the code
   */
  def timeTrack[V](operationName: String, timeUnit: ChronoUnit = SECONDS)(code: => V): V = {
    val start = now()
    val result = code
    val finish = now()
    logJob(
      name = operationName,
      timeUnit = timeUnit,
      length = timeUnit.between(start, finish)
    )
    result
  }

  def timeTrackReturningDuration[V](operationName: String, timeUnit: ChronoUnit = MILLIS)(code: => V): (V, Long) = {
    val start = now()
    val result = code
    val finish = now()
    val length = timeUnit.between(start, finish)
    logJob(
      name = operationName,
      timeUnit = timeUnit,
      length = length
    )
    result -> length
  }

}
