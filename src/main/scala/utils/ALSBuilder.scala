package ir.ac.usc
package utils

import conf.ALSConfig
import org.apache.spark.mllib.recommendation.ALS


/**
 * Builder object for als model creation.
 */
object ALSBuilder {

  /**
   * Creates a new ALS for the given config
   * @param config configurations to build als from
   * @return ALS object to fit ratings in
   */
  def forConfig(config: ALSConfig): ALS =
    new ALS()
      .setRank(config.rank)
      .setSeed(config.seed)
      .setAlpha(config.alpha)
      .setBlocks(config.block)
      .setLambda(config.lambda)
      .setIterations(config.iterations)
      .setImplicitPrefs(config.implicitPreferences)

}
