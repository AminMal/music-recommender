package ir.ac.usc

import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}
import akka.actor.ActorSystem
import controllers.ApplicationStatusController.Responses._
import models.responses.{ErrorBody, FailureResponse, ResponseMessage, SuccessResponse}
import models.{Song, SongDTO, User}

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import controllers.RecommendationController.Responses._

import akka.stream.Materializer
import controllers.ContextManagerActor.Messages.AddUserRating

import conf.ALSConfig

object Bootstrap {

  Logger.getLogger("org").setLevel(Level.ERROR)

  final val spark = SparkSession
    .builder()
    .appName("scommender")
    .config("spark.master", "local[4]")  // todo, this needs to be removed in production
    .getOrCreate()

  import utils.SparkFunctions._
  spark.udf.register("geterr", getError)
  spark.udf.register("getstate", getState)
  spark.udf.register("fMeasure", fMeasure)

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

    implicit val errorBodyFormatter: RootJsonFormat[ErrorBody] = jsonFormat2(ErrorBody.apply)
    implicit def unsuccessResponseFormatter: RootJsonFormat[FailureResponse] = jsonFormat2(FailureResponse.apply)
    implicit val addUserRatingFormatter: RootJsonFormat[AddUserRating] = jsonFormat3(AddUserRating.apply)
    implicit val messageFormatter: RootJsonFormat[ResponseMessage] = jsonFormat1(ResponseMessage.apply)
    implicit val addUserFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
    implicit val songDtoFormatter: RootJsonFormat[SongDTO] = jsonFormat6(SongDTO.apply)
    implicit val alsConfigFormatter: RootJsonFormat[ALSConfig] = jsonFormat7(ALSConfig)
  }
}
