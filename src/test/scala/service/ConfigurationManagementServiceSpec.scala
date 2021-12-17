package ir.ac.usc
package service

import conf.ALSConfig
import org.scalatest.{Matchers, AsyncWordSpec}

class ConfigurationManagementServiceSpec extends AsyncWordSpec with Matchers {

  val provider = new ServiceProvider("config-management-service")
  import provider._

  "configuration management service" should {
    "return latest config as ALSConfig" in {
      service.configurationManagementService.getLatestConfig
        .map(res => assert(res.isInstanceOf[ALSConfig]))
    }

    "should update application configurations and return latest config" in {
      val config: ALSConfig = ALSConfig(4, 1, 0D, 2, 9, 9L, implicitPreferences = true)
      for {
        updateConfigResult <- service.configurationManagementService.updateConfig(config, forced = true)
        latestConfigInActor <- service.configurationManagementService.getLatestConfig
      } yield {
        assert(config == updateConfigResult.latestConf && config == latestConfigInActor)
      }
    }

  }

}
