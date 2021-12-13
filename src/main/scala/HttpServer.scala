package ir.ac.usc

import controllers.{ApplicationStatusController, ConfigManagerActor, ContextManagerActor, PerformanceEvaluatorActor, RecommendationController, RecommenderManagerActor}

import akka.http.scaladsl.Http
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import conf.ServerConfig

import akka.actor.{ActorRef, Props}
import controllers.RecommenderManagerActor.Messages._

import akka.pattern.ask

import scala.concurrent.Future
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

object HttpServer {

  import Bootstrap.{system, materializer}
  implicit val timeout: Timeout = Timeout(60, TimeUnit.SECONDS)

  val route: Route =
    ApplicationStatusController.routes ~
      RecommendationController.routes ~
      ContextManagerActor.routes

  import system.dispatcher
  lazy val runServer: () => Future[Http.ServerBinding] = () => Http().newServerAt(
    ServerConfig.serverInterface, ServerConfig.serverPort
  )
    .withMaterializer(materializer)
    .bind(route)
    .map(_.addToCoordinatedShutdown(10.seconds))

  println(s"--- started server on port ${ServerConfig.serverPort} ---")

  val contextManagerActor: ActorRef = system.actorOf(Props[ContextManagerActor])
  val recommenderManager: ActorRef = system.actorOf(Props[RecommenderManagerActor])
  val applicationController: ActorRef = system.actorOf(Props[ApplicationStatusController])
  val configManagerActor: ActorRef = system.actorOf(Props[ConfigManagerActor])
  val performanceTestActor: ActorRef = system.actorOf(PerformanceEvaluatorActor.props)

  def newRecommenderActor(): Future[ActorRef] =
    (recommenderManager ? NewRecommenderActor).mapTo[ActorRef]
}
