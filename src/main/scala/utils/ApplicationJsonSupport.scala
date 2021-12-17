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


trait ApplicationJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit class StringOps(value: String) {
    def isNumeric: Boolean = {
      value matches "\\d+[.]?\\d*"
    }
  }

  implicit val modelActivationResponseFormatter: RootJsonFormat[ModelActivationResponse] = jsonFormat1(ModelActivationResponse)
  implicit val healthCheckResponseFormatter: RootJsonFormat[HealthCheckResponse] = jsonFormat2(HealthCheckResponse)
  implicit val songFormatter: RootJsonFormat[Song] = jsonFormat3(Song.apply)
  implicit val metaFormatter: RootJsonFormat[Meta] = jsonFormat1(Meta.apply)
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

  implicit val dfJsonFormatter: RootJsonWriter[DataFrame] = obj =>  {
    val mapArray = obj.collect().map { row =>
      row.getValuesMap[Any](obj.columns).map {
        case (key, value) => key -> value.toString
      }
    }.toSeq

    val jsonedObjects = mapArray.map { objMapped =>
      JsObject.apply(
        objMapped.map {
          case (key, value) => key -> {
            if (value.isNumeric)
              JsNumber(BigDecimal(value))
            else JsString(value)
          }
        }
      )
    }

    if (jsonedObjects.length == 1) {
      jsonedObjects.head
    } else {
      JsArray(jsonedObjects.toVector)
    }
  }
}

object ApplicationJsonSupport extends ApplicationJsonSupport
