package scommender

import server.RoutesModule
import service.ServiceModule

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object Bootstrap {
  final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local[4]") // todo, this needs to be removed in production
    .getOrCreate()

  Logger.getLogger("org").setLevel(Level.ERROR)
  val appConfig: Config = ConfigFactory.load()

  import utils.SparkFunctions._

  spark.udf.register("geterr", getError)
  spark.udf.register("getstate", getState)
  spark.udf.register("fMeasure", fMeasure)

  println("--- initialized spark session ---")

  /**
   * ActorSystem is required to create actors and server
   */
  implicit val actorSystem: ActorSystem = ActorSystem("scommender")
  implicit val materializer: Materializer = Materializer.matFromSystem

  import HttpServer.timeout
  val services: ServiceModule = ServiceModule.forSystem(actorSystem)

  val routes: RoutesModule = new RoutesModule(services)(ActorSystem("route-handler"))
}
