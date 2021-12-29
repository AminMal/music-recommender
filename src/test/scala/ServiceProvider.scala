package ir.ac.usc

import akka.actor.ActorSystem
import akka.stream.Materializer
import controllers.ContextManagerActor.Messages.AddUserRating
import models.{SongDTO, User}
import service.ServiceModule

import akka.Done
import utils.box.{Box, BoxF}

import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import scala.concurrent.Future


class ServiceProvider(systemName: String) extends MockitoSugar {

  implicit val actorSystem: ActorSystem = ActorSystem(systemName)
  val mat: Materializer = Materializer.matFromSystem

  val service: ServiceModule = spy(ServiceModule.forSystem(actorSystem)(HttpServer.timeout))

  private val defaultResult: BoxF[Done] = new BoxF[Done](Box(Future.successful(Done)))(service.system.dispatcher)

  when(service.contextManagerService.addSong(any[SongDTO])) thenReturn defaultResult

  when(service.contextManagerService.addUser(any[User])) thenReturn defaultResult

  when(service.contextManagerService.addUserRating(any[AddUserRating])) thenReturn defaultResult

  when(service.contextManagerService.addUserRating(any[Long], any[Long], any[Double])) thenReturn defaultResult

}
