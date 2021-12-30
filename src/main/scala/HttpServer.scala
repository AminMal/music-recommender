package scommender

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.util.Timeout
import server.RoutesModule

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object HttpServer {

  implicit val timeout: Timeout = 60.seconds

  def runServer(
                 interface: String,
                 port: Int,
                 routesModule: RoutesModule
               )(implicit system: ActorSystem): Future[Http.ServerBinding] = {
    import system.dispatcher
    Http().newServerAt(
      interface, port
    )
      .bind(routesModule.routes)
      .map(_.addToCoordinatedShutdown(10.seconds))(system.dispatcher)
      .map { binding =>
        println(s"--- started server on port $port ---")
        binding
      }
  }
}
