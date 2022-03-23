package scommender
package conf

import com.typesafe.config.Config
import Bootstrap.appConfig
import scala.util.Try

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

object ALSConfig {
  def fromConfig(config: Config): ALSConfig = apply(
    rank = config.getInt("rank"),
    iterations = config.getInt("iterations"),
    lambda = Try(config.getDouble("lambda")).getOrElse(0.01D),
    alpha = Try(config.getDouble("alpha")).getOrElse(1.0D),
    block = Try(config.getInt("block")).getOrElse(-1),
    implicitPreferences = Try(config.getBoolean("implicit-prefs")).getOrElse(false)
  )

  def fromConfigPath(path: String): ALSConfig = fromConfig(appConfig.getConfig(path))
}
