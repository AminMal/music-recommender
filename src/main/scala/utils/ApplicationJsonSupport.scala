package ir.ac.usc
package utils

import conf.ALSConfig
import controllers.ApplicationStatusController.Responses.{HealthCheckResponse, ModelActivationResponse}
import controllers.ContextManagerActor.Messages.AddUserRating
import models.responses._
import models._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.spark.sql.DataFrame
import spray.json._

import scala.reflect.ClassTag


/**
 * This trait holds all the json formatters for models used in domain.
 */
trait ApplicationJsonSupport extends SprayJsonSupport with JsonSnakecaseFormatSupport {

  implicit val modelActivationResponseFormatter: RootJsonFormat[ModelActivationResponse] = jsonFormat1(ModelActivationResponse)
  implicit val healthCheckResponseFormatter: RootJsonFormat[HealthCheckResponse] = jsonFormat2(HealthCheckResponse)
  implicit val metaFormatter: RootJsonFormat[Meta] = jsonFormat1(Meta.apply)
  implicit val songDTOFormatter: RootJsonFormat[SongDTO] = jsonFormat6(SongDTO.apply)
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
  implicit val alsConfigFormatter: RootJsonFormat[ALSConfig] = jsonFormat7(ALSConfig)
  implicit val dfJsonFormatter: RootJsonWriter[DataFrame] = df => {
    val jsonArray = df.toJSON
    if (df.take(2).length == 1) {
      JsonParser(jsonArray.head())
    } else {
      JsonParser(jsonArray.collect().mkString("[", ",", "]"))
    }
  }

  implicit def scommenderResponseWriter[T : ClassTag](
                                                       implicit formatter: RootJsonFormat[T]
                                                     ): RootJsonWriter[ScommenderResponse[T]] = {
    case result: SuccessResponse[T] => successResponseFormatter(formatter).write(result)
    case failed: FailureResponse => unsuccessResponseFormatter.write(failed)
  }
}

/**
 * ApplicationJsonSupport companion object, so that can import object instead of extending trait
 */
object ApplicationJsonSupport extends ApplicationJsonSupport
