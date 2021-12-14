package ir.ac.usc
package service.algebra

import controllers.ContextManagerActor.Responses.CMOperationResult
import models.{SongDTO, User}

import controllers.ContextManagerActor.Messages.AddUserRating
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.Future

trait ContextManagerServiceAlgebra {

  def updateModel(): Future[CMOperationResult]
  def getLatestModel: Future[Option[MatrixFactorizationModel]]
  def addUserRating(userId: Long, songId: Long, rating: Double): Future[CMOperationResult]
  def addUserRating(request: AddUserRating): Future[CMOperationResult]
  def addUser(user: User): Future[CMOperationResult]
  def addSong(song: SongDTO): Future[CMOperationResult]


}
