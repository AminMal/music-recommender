package ir.ac.usc

import akka.http.scaladsl.Http
import akka.util.Timeout
import scala.concurrent.Future
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

object HttpServer {

  implicit val timeout: Timeout = Timeout(60, TimeUnit.SECONDS)
  import Bootstrap.{actorSystem, materializer, routesAndService, appConfig}
  import actorSystem.dispatcher

  val interface: String = appConfig.getString("scommender.server.interface")
  val port: Int         = appConfig.getInt("scommender.server.port")

  lazy val runServer: () => Future[Http.ServerBinding] = () => Http().newServerAt(
    interface = interface, port = port
  )
    .withMaterializer(materializer)
    .bind(routesAndService.routes)
    .map(_.addToCoordinatedShutdown(10.seconds))(actorSystem.dispatcher)
    .map { binding =>
      println(s"--- started server on port $port ---")
      binding
    }
}
