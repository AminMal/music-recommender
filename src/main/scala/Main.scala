package scommender

import Bootstrap.{actorSystem, appConfig, routes}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.Try

object Main extends App {
  val interface: String = appConfig.getString("scommender.server.interface")
  val port: Int = appConfig.getInt("scommender.server.port")
  val httpServerThreadsCount =
    Try(appConfig.getInt("scommender.server.numThreads"))
      .getOrElse(Runtime.getRuntime.availableProcessors())

  HttpServer.runServer(
    interface = interface, port = port,
    routesModule = routes
  )(system = actorSystem, ec = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(httpServerThreadsCount)
  ))

}
