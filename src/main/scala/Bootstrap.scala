package scommender

import server.RoutesModule
import service.ServiceModule

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import utils.DataFrameProvider


object Bootstrap {
  val appConfig: Config = ConfigFactory.load()
  val sparkThreads: Int = appConfig.getInt("scommender.spark.num-threads")

  Logger.getLogger("org").setLevel(Level.ERROR)

  final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", s"local[$sparkThreads]") // todo, this needs to be removed in production
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

  val dataframeProducer: () => DataFrameProvider = DataFrameProvider.producer(spark)

  import HttpServer.timeout
  val services: ServiceModule = ServiceModule.forSystem(actorSystem, dataframeProducer)

  val routes: RoutesModule = new RoutesModule(services, dataframeProducer.apply())(ActorSystem("route-handler"))
}
