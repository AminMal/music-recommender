package ir.ac.usc

import akka.actor.ActorSystem
import akka.stream.Materializer
import controllers.ContextManagerActor.Messages.AddUserRating
import controllers.ContextManagerActor.Responses.{CMOperationResult, SuccessfulOperation}
import models.{SongDTO, User}
import service.ServiceModule
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._

import scala.concurrent.Future


class ServiceProvider(systemName: String) extends MockitoSugar {

  implicit val actorSystem: ActorSystem = ActorSystem(systemName)
  val mat: Materializer = Materializer.matFromSystem

  val service: ServiceModule = ServiceModule.forSystem(actorSystem)(HttpServer.timeout)

  private val defaultResult: Future[CMOperationResult] = Future.successful(SuccessfulOperation)

  when(service.contextManagerService.addSong(any[SongDTO])) thenReturn defaultResult

  when(service.contextManagerService.addUser(any[User])) thenReturn defaultResult

  when(service.contextManagerService.addUserRating(any[AddUserRating])) thenReturn defaultResult

  when(service.contextManagerService.addUserRating(any[Long], any[Long], any[Double])) thenReturn defaultResult

}
