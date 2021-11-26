package ir.ac.usc
package conf

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/**
 * Default configuration object for ALS algorithm
 */
object ALSDefaultConf extends ALSConfig(
  rank = 4, iterations = 10, lambda = 0.01, block = -1, seed = 12345L, alpha = 0.8
) {

  val updateInterval: FiniteDuration = FiniteDuration(2, TimeUnit.MINUTES)

}
