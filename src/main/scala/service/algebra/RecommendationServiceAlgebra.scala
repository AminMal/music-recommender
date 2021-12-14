package ir.ac.usc
package service.algebra

import scala.concurrent.Future
import models.RecommendationResult

trait RecommendationServiceAlgebra {

  def getRecommendations(userId: Int, count: Int = 6): Future[RecommendationResult]

}
