package ir.ac.usc
package service

import controllers.ApplicationStatusController.Responses._
import org.scalatest.Matchers

class ApplicationStatusServiceSpec extends BoxFWordSpecLike with Matchers {

  val provider = new ServiceProvider("application-status-service")
  import provider._

  "application status service" should {
    "return success" inBox {
      service.applicationStatusService.health().map { response =>
        println(s"response is $response")
        assert(response.isInstanceOf[HealthCheckResponse])
      }
    }
  }


}
