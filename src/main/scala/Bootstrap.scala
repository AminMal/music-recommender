package ir.ac.usc

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}
import conf.{RecommenderDataPaths => Paths}

import akka.actor.ActorSystem
import controllers.ApplicationStatusController.Responses._
import models.responses.{ErrorBody, FailureResponse, SuccessResponse}
import models.Song

import org.apache.spark.mllib.recommendation.Rating
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import controllers.RecommendationController.Responses._

import akka.stream.Materializer
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}

object Bootstrap {

  Logger.getLogger("org").setLevel(Level.ERROR)

  final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local")  // todo, this needs to be removed in production
    .getOrCreate()

  println("--- initialized spark session ---")

  /**
   * ActorSystem is requires to create actors and server
   */
  implicit val system: ActorSystem = ActorSystem("scommender")
  implicit val materializer: Materializer = Materializer.matFromSystem

  object JsonImplicits {
    implicit val modelActivationResponseFormatter: RootJsonFormat[ModelActivationResponse] = jsonFormat1(ModelActivationResponse)
    implicit val healthCheckResponseFormatter: RootJsonFormat[HealthCheckResponse] = jsonFormat2(HealthCheckResponse)
    implicit val songFormatter: RootJsonFormat[Song] = jsonFormat3(Song.apply)
    implicit val recommendationResultFormatter: RootJsonFormat[RecommendationResult] = jsonFormat3(RecommendationResult)
    implicit def successResponseFormatter[D](
                                              implicit dataFormatter: RootJsonFormat[D]
                                            ): RootJsonFormat[SuccessResponse[D]] =
      jsonFormat2(SuccessResponse[D])

    implicit val errorBodyFormatter: RootJsonFormat[ErrorBody] = jsonFormat2(ErrorBody)
    implicit def unsuccessResponseFormatter: RootJsonFormat[FailureResponse] = jsonFormat2(FailureResponse)
  }
}
