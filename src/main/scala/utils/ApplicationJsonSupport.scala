package scommender
package utils

import conf.ALSConfig
import controllers.ApplicationStatusController.Responses.{HealthCheckResponse, ModelActivationResponse}
import controllers.ContextManagerActor.Messages.AddUserRating
import models._
import models.responses._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.spark.sql.DataFrame
import spray.json._


/**
 * This trait holds all the json formatters for models used in domain.
 */
trait ApplicationJsonSupport extends SprayJsonSupport with JsonSnakecaseFormatSupport with DateTimeFormatSupport {

  implicit val modelActivationResponseFormatter: RootJsonFormat[ModelActivationResponse] = jsonFormat1(ModelActivationResponse)
  implicit val healthCheckResponseFormatter: RootJsonFormat[HealthCheckResponse] = jsonFormat2(HealthCheckResponse)
  implicit val metaFormatter: RootJsonFormat[Meta] = jsonFormat1(Meta.apply)
  implicit val songDTOFormatter: RootJsonFormat[SongDTO] = jsonFormat6(SongDTO.apply)
  implicit val recommendationResultFormatter: RootJsonFormat[RecommendationResult] = jsonFormat3(RecommendationResult)
  implicit val alsConfigFormatter: RootJsonFormat[ALSConfig] = jsonFormat7(ALSConfig)
  implicit val diagnosticsPerformanceFormatter: RootJsonFormat[ModelPerformanceResult] = jsonFormat4(ModelPerformanceResult)
  implicit val diagnosticReportFormatter: RootJsonFormat[DiagnosticsReport] =
    jsonFormat8(DiagnosticsReport)

  implicit def successResponseFormatter[D](
                                            implicit dataWriter: RootJsonWriter[D]
                                          ): RootJsonWriter[SuccessResponse[D]] = obj => {
    JsObject(
      "success" -> JsBoolean(obj.success),
      "data" -> dataWriter.write(obj.data)
    )
  }

  implicit def successResponseReader[D](
                                         implicit dataReader: RootJsonReader[D]
                                       ): RootJsonReader[SuccessResponse[D]] = json => {
    SuccessResponse(success = true, dataReader.read(json))
  }

  implicit val errorBodyFormatter: RootJsonFormat[ErrorBody] = jsonFormat2(ErrorBody.apply)

  implicit def unsuccessfulResponseFormatter: RootJsonFormat[FailureResponse] = jsonFormat2(FailureResponse.apply)

  implicit val addUserRatingFormatter: RootJsonFormat[AddUserRating] = jsonFormat3(AddUserRating.apply)
  implicit val messageFormatter: RootJsonFormat[ResponseMessage] = jsonFormat1(ResponseMessage.apply)
  implicit val addUserFormat: RootJsonFormat[User] = jsonFormat3(User.apply)

  implicit val dfJsonFormatter: RootJsonWriter[DataFrame] = df => {
    val jsonArray = df.toJSON
    if (df.take(2).length == 1) {
      JsonParser(jsonArray.head())
    } else {
      JsonParser(jsonArray.collect().mkString("[", ",", "]"))
    }
  }

  implicit def scommenderResponseWriter[T](
                                                      implicit formatter: RootJsonWriter[T]
                                                    ): RootJsonWriter[ScommenderResponse[T]] = {
    case result: SuccessResponse[T] => successResponseFormatter(formatter).write(result)
    case failed: FailureResponse => unsuccessfulResponseFormatter.write(failed)
  }
}

/**
 * ApplicationJsonSupport companion object, so that can import object instead of extending trait
 */
object ApplicationJsonSupport extends ApplicationJsonSupport
