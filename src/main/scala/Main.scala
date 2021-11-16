package ir.ac.usc

import utils.ALSBuilder
import Bootstrap.DataFrames.ratingsRDD
import Bootstrap.{serverBinding, system}
import conf.ALSDefaultConf
import controllers.{ApplicationStatusController, RecommendationController}

import models.RecommenderFactorizationModel
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

import scala.concurrent.Future


object Main extends App {

  import system.dispatcher

  serverBinding
  val matrixModelFuture = Future {
    val model: MatrixFactorizationModel = ALSBuilder.forConfig(ALSDefaultConf).run(ratingsRDD)
    val recommenderMatrixModel = RecommenderFactorizationModel.fromMatrixModel(model)
    println(s"--- finished training model ---")
    Bootstrap.applicationController ! ApplicationStatusController.Messages.ModelActivated
    Bootstrap.recommenderActors.foreach { recommendActor =>
      recommendActor ! RecommendationController.Messages.UpdateContext(recommenderMatrixModel.copy())
    }
    model
  }


}
