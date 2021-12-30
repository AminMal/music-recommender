package scommender

import Bootstrap.{actorSystem, appConfig, routes}

object Main extends App {

  val interface: String = appConfig.getString("scommender.server.interface")
  val port: Int = appConfig.getInt("scommender.server.port")

  HttpServer.runServer(
    interface = interface, port = port,
    routesModule = routes
  )

}
