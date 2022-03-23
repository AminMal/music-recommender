package scommender
package models

import conf.ALSConfig
import java.time.LocalDateTime


case class DiagnosticsReport(
                      session: String,
                      registeredAt: LocalDateTime,
                      config: Option[ALSConfig] = None,
                      trainingDurationMillis: Option[Long] = None,
                      loadingDurationMillis: Option[Long] = None,
                      updatedAt: Option[LocalDateTime] = None,
                      servedRecommendationRequests: Int = 0,
                      performance: Option[ModelPerformanceResult] = None
                      )

case class ModelPerformanceResult(
                                 precision: BigDecimal,
                                 recall: BigDecimal,
                                 fmeasure: BigDecimal,
                                 rmse: BigDecimal
                                 )
