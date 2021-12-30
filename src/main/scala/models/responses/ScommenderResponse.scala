package scommender
package models.responses

import akka.http.scaladsl.model.StatusCode

trait ScommenderResponse[+T] {

  def status: StatusCode

  def map[V](f: T => V): ScommenderResponse[V] = this match {
    case failed@FailureResponse(_, _) =>
      failed
    case SuccessResponse(_, data) =>
      SuccessResponse[V](data = f(data))
  }

  def flatMap[V](f: T => ScommenderResponse[V]): ScommenderResponse[V] = this match {
    case failed@FailureResponse(_, _) =>
      failed
    case SuccessResponse(_, data) =>
      f(data)
  }

}
