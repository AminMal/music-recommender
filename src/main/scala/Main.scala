package ir.ac.usc

object Main extends App {
  HttpServer.runServer()
  Bootstrap.services.initiate(Bootstrap.actorSystem.dispatcher)
}
