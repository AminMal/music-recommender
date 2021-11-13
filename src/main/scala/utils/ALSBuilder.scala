package ir.ac.usc
package utils

import org.apache.spark.mllib.recommendation.ALS

object ALSBuilder {

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
