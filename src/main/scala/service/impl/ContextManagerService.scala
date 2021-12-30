package ir.ac.usc
package service.impl

import controllers.ContextManagerActor.Messages._
import controllers.ContextManagerActor.Responses._
import models.{SongDTO, User}
import service.algebra.ContextManagerServiceAlgebra
import utils.box.{BoxF, BoxSupport}

import akka.Done
import akka.actor.ActorRef
import akka.util.Timeout
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.ExecutionContext

class ContextManagerService(contextManagerActor: ActorRef)(
  implicit timeout: Timeout,
  ec: ExecutionContext
) extends ContextManagerServiceAlgebra with BoxSupport {

  override def updateModel(): Unit = {
    contextManagerActor ! UpdateModel
  }

  override def getLatestModel: BoxF[Option[MatrixFactorizationModel]] = {
    (contextManagerActor ??[Option[MatrixFactorizationModel]] GetLatestModel)
  }

  override def addUserRating(userId: Long, songId: Long, rating: Double): BoxF[Done] = {
    val request = AddUserRating(userId, songId, rating)
    (contextManagerActor ??[CMOperationResult] request).map(_ => Done)
  }

  override def addUserRating(request: AddUserRating): BoxF[Done] = {
    (contextManagerActor ??[CMOperationResult] request).map(_ => Done)
  }

  override def addUser(user: User): BoxF[Done] = {
    (contextManagerActor ??[CMOperationResult] AddUser(user)).map(_ => Done)
  }

  override def addSong(song: SongDTO): BoxF[Done] = {
    (contextManagerActor ??[CMOperationResult] AddSong(song)).map(_ => Done)
  }
}
