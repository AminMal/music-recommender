package scommender
package server

import conf.ALSConfig

import akka.http.scaladsl.model.StatusCodes
import models.responses.{ResponseMessage, SuccessResponse}

import scala.util.Random

class ConfigurationRouteSpec extends RouteProvider("configuration-route-spec") {

  import provider._

  def newConfig: ALSConfig = ALSConfig.apply(
    rank = Random.nextInt(30),
    iterations = Random.nextInt(10),
    lambda = Random.nextDouble(),
    alpha = Random.nextDouble(),
    block = -1,
    seed = Random.nextLong()
  )

  "config manager route service" should {

    "update application configurations" in {
      Put("/conf/update", content = newConfig) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessResponse[ResponseMessage]].data.message shouldEqual "configurations updated successfully!"
      }
    }

  }

}
