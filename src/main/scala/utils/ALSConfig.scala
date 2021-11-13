package ir.ac.usc
package utils

case class ALSConfig(
                    rank: Int,
                    iterations: Int,
                    lambda: Double,
                    alpha: Double,
                    block: Int,
                    seed: Long,
                    implicitPreferences: Boolean = false
                    )
