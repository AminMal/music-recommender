package ir.ac.usc
package models.responses

import akka.http.scaladsl.model.StatusCode

trait ScommenderResponse[+T] {

  def status: StatusCode

}
