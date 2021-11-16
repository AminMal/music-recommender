package ir.ac.usc
package models

import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD

import scala.concurrent.{ExecutionContext, Future}

case class RecommenderFactorizationModel(
                                          override val rank: Int,
                                          override val userFeatures: RDD[(Int, Array[Double])],
                                          override val productFeatures: RDD[(Int, Array[Double])]
                                        )(implicit ec: ExecutionContext) extends MatrixFactorizationModel(rank, userFeatures, productFeatures) {

  def recommendProductsFuture(user: Int, num: Int): Future[Seq[Rating]] =
    Future[Seq[Rating]](recommendProducts(user, num).toSeq)

}

object RecommenderFactorizationModel {
  def fromMatrixModel(model: MatrixFactorizationModel)(
                     implicit ex: ExecutionContext
  ): RecommenderFactorizationModel =
    RecommenderFactorizationModel(model.rank, model.userFeatures, model.productFeatures)
}
