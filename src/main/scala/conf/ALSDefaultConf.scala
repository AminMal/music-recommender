package ir.ac.usc
package conf

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/**
 * Default configuration object for ALS algorithm
 */
object ALSDefaultConf extends ALSConfig(
  rank = 8, iterations = 12, lambda = 0.01, block = -1, alpha = 1.0
) {

  val updateInterval: FiniteDuration = FiniteDuration(5, TimeUnit.MINUTES)

}
