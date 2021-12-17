package ir.ac.usc
package server

import exception.ScommenderException

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, _}

object ApplicationExceptionHandler {

  import utils.ApplicationJsonSupport._

  val handler: ExceptionHandler = ExceptionHandler {
    case se: ScommenderException => complete(
      status = se.status, v = se.toResponseBody
    )
  }

}
