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

class ContextManagerService(contextManagerActor: ActorRef)(
                           implicit timeout: Timeout
) extends ContextManagerServiceAlgebra {

  override def updateModel(): Unit = {
    contextManagerActor ! UpdateModel
  }

  override def getLatestModel: Future[Option[MatrixFactorizationModel]] = {
    (contextManagerActor ? GetLatestModel).mapTo[Option[MatrixFactorizationModel]]
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
