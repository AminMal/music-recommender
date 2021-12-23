package ir.ac.usc
package service.algebra

import scala.concurrent.Future
import models.RecommendationResult


/**
 * Service representing recommender manager actor features
 */
trait RecommendationServiceAlgebra {

  /**
   * recommend songs for user
   * @param userId user to recommend songs for
   * @param count number of recommendations
   * @return an object of recommendation result
   */
  def getRecommendations(userId: Int, count: Int = 6): Future[RecommendationResult]

}
