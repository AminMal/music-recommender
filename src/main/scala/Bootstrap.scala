package ir.ac.usc

import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}
import akka.actor.ActorSystem
import akka.stream.Materializer
import server.RoutesModule
import service.ServiceModule

import com.typesafe.config.{Config, ConfigFactory}

object Bootstrap {

  val appConfig: Config = ConfigFactory.load()

  Logger.getLogger("org").setLevel(Level.ERROR)

  final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local[8]")  // todo, this needs to be removed in production
    .getOrCreate()

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

  val services: ServiceModule = ServiceModule.forSystem(actorSystem)

  val routes: RoutesModule = new RoutesModule(services)
}
