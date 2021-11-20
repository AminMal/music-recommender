package ir.ac.usc
package conf

/**
 * Default configuration object for ALS algorithm
 */
object ALSDefaultConf extends ALSConfig(
  rank = 4, iterations = 10, lambda = 0.01, block = -1, seed = 12345L, alpha = 0.8
)
