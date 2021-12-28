package ir.ac.usc
package service

import controllers.ApplicationStatusController.Responses._
import org.scalatest.{Matchers, AsyncWordSpec}

class ApplicationStatusServiceSpec extends AsyncWordSpec with Matchers {

  val provider = new ServiceProvider("application-status-service")
  import provider._
//
//  "application status service" should {
//    "return success" in {
//      service.applicationStatusService.health().map { response =>
//        println(s"response is $response")
//        assert(response.isInstanceOf[HealthCheckResponse])
//      }
//    }
//  }


}
