package ir.ac.usc
package conf

/**
 * Configuration class in order to create MatrixFactorizationModel more easily
 *
 * @param rank                rank parameter for ALS
 * @param iterations          number of iterations for algorithm
 * @param lambda              lambda parameter for ALS
 * @param alpha               alpha parameter for ALS
 * @param block               number of blocks, -1 to auto configuration
 * @param seed                seed parameter for ALS
 * @param implicitPreferences implicit preferences for ALS algorithm
 */
case class ALSConfig(
                      rank: Int,
                      iterations: Int,
                      lambda: Double,
                      alpha: Double,
                      block: Int,
                      seed: Long = System.nanoTime(),
                      implicitPreferences: Boolean = false
                    )
