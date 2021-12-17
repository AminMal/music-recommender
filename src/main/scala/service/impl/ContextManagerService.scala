package ir.ac.usc
package service.impl

import controllers.ContextManagerActor.Messages._
import controllers.ContextManagerActor.Responses._
import models.{SongDTO, User}
import service.algebra.ContextManagerServiceAlgebra

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import java.util.concurrent.TimeUnit
import scala.concurrent.Future

class ContextManagerService(contextManagerActor: ActorRef) extends ContextManagerServiceAlgebra {

  private implicit val baseTimeout: Timeout = Timeout(50, TimeUnit.SECONDS)
  override def updateModel(): Unit = {
    contextManagerActor ! UpdateModel
  }

  override def getLatestModel: Future[Option[MatrixFactorizationModel]] = {
    implicit val getModelTimeout: Timeout = Timeout(2, TimeUnit.SECONDS)
    (contextManagerActor ? GetLatestModel)(getModelTimeout).mapTo[Option[MatrixFactorizationModel]]
  }

  override def addUserRating(userId: Long, songId: Long, rating: Double): Future[CMOperationResult] = {
    (
      contextManagerActor ? AddUserRating(
        userId = userId,
        songId = songId,
        rating = rating
      )
    ).mapTo[CMOperationResult]
  }

  override def addUserRating(request: AddUserRating): Future[CMOperationResult] = {
    (
      contextManagerActor ? request
    ).mapTo[CMOperationResult]
  }

  override def addUser(user: User): Future[CMOperationResult] = {
    (
      contextManagerActor ? AddUser(user)
    )
      .mapTo[CMOperationResult]
  }

  override def addSong(song: SongDTO): Future[CMOperationResult] = {
    (
      contextManagerActor ? AddSong(song)
    )
      .mapTo[CMOperationResult]
  }
}
