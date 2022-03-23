package scommender

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.util.Timeout
import server.RoutesModule

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import org.apache.log4j.Logger

object HttpServer {

  implicit val timeout: Timeout = 60.seconds

  private val logger = Logger.getLogger("ScommenderServer")

  def runServer(
                 interface: String,
                 port: Int,
                 routesModule: RoutesModule
               )(implicit system: ActorSystem, ec: ExecutionContext): Future[Http.ServerBinding] = {
    Http().newServerAt(
      interface, port
    )
      .bind(routesModule.routes)
      .map(_.addToCoordinatedShutdown(10.seconds))
      .map { binding =>
        logger.info(s"started server on port: $port")
        binding
      }
  }
}
