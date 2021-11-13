package ir.ac.usc

import utils.ALSConfig

object ALSDefaultConf extends ALSConfig (
  rank = 20, iterations = 15, lambda = 0.1, block = -1, seed = 12345L, alpha = 1.00
)
