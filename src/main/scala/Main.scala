package ir.ac.usc

import utils.ALSBuilder
import Bootstrap.DataFrames.ratingsRDD
import Bootstrap.system
import conf.ALSDefaultConf
import controllers.{ApplicationStatusController, RecommendationController}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.Future


object Main extends App {

  import system.dispatcher
  import HttpServer.{applicationController, recommenderActors}

  HttpServer.serverBinding
  val matrixModelFuture = Future {
    val model: MatrixFactorizationModel = ALSBuilder.forConfig(ALSDefaultConf).run(ratingsRDD)
    println(s"--- finished training model ---")
    applicationController ! ApplicationStatusController.Messages.ModelActivated
    recommenderActors.foreach { recommendActor =>
      recommendActor ! RecommendationController.Messages.UpdateContext(model)
    }
    model
  }


}
