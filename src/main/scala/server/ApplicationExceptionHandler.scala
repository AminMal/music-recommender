package scommender
package server

import exception.ScommenderException

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler

import scala.util.control.NonFatal


/**
 * Singleton object that contains exception handler directive
 */
object ApplicationExceptionHandler {

  import utils.ApplicationJsonSupport._

  val handler: ExceptionHandler = ExceptionHandler {
    case se: ScommenderException => complete(
      status = se.status, v = se.toResponseBody
    )
    case other =>
      other.printStackTrace()
      val se = ScommenderException.adopt(other)
      complete(status = se.status, v = se.toResponseBody)
  }

}
