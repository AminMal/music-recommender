package scommender
package controllers

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ApplicationStatusControllerSpec
  extends TestKit(ActorSystem("apllication-status-system"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

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