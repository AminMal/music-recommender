package ir.ac.usc
package conf

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/**
 * Default configuration object for ALS algorithm
 */
object ALSDefaultConf extends ALSConfig(
  rank = 10, iterations = 10, lambda = 0.01, block = -1, seed = 12345L, alpha = 1.0
) {

  val updateInterval: FiniteDuration = FiniteDuration(5, TimeUnit.MINUTES)

}
