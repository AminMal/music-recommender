package ir.ac.usc

import controllers.{ApplicationStatusController, ContextManagerActor, RecommendationController}

import akka.http.scaladsl.Http
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import conf.ServerConfig

import akka.actor.{ActorRef, Props}

import scala.concurrent.Future
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.util.Random

object HttpServer {

  import Bootstrap.{system, materializer}
  implicit val timeout: Timeout = Timeout(60, TimeUnit.SECONDS)
  private val applicationHandlerRoutes = ApplicationStatusController.routes
  private val recommenerRoutes = RecommendationController.routes

  val route: Route =
    applicationHandlerRoutes ~
      recommenerRoutes

  import system.dispatcher
  lazy val runServer: () => Future[Http.ServerBinding] = () => Http().newServerAt(
    ServerConfig.serverInterface, ServerConfig.serverPort
  )
    .withMaterializer(materializer)
    .bind(route)
    .map(_.addToCoordinatedShutdown(10.seconds))

  println(s"--- started server on port ${ServerConfig.serverPort} ---")

  val contextManagerActor: ActorRef = system.actorOf(Props[ContextManagerActor])
  val applicationController: ActorRef = system.actorOf(Props[ApplicationStatusController])
  val recommenderActors: Seq[ActorRef] = (1 to 51).toSeq.map { index =>
    system.actorOf(Props[RecommendationController], s"recommender-actor-$index")
  }
  def recommenderActor: ActorRef = {
    val randomIndex = Random.nextInt(50)  // todo, replace with some logic
    recommenderActors(randomIndex % 50)
  }
}
