package ir.ac.usc
package controllers

import akka.actor.Props

class ApplicationStatusControllerSpec
  extends ScommenderBaseTestkit {

  "status controller" must {
    "return valid health check response" in {
      import ApplicationStatusController.Messages._
      import ApplicationStatusController.Responses._
      val statusController = system.actorOf(Props[ApplicationStatusController])
      statusController ! HealthCheck
      expectMsgType[HealthCheckResponse]
    }
  }
}