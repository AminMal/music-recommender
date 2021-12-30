package ir.ac.usc
package controllers

import controllers.RecommendationController.Messages.UpdateContext
import utils.box.BoxSupport

import akka.actor.{Actor, ActorRef, Props}


/**
 * This actor controls recommendation requests and slave actors.
 */
class RecommenderManagerActor extends Actor with BoxSupport {

  import Bootstrap.services.contextManagerService
  import controllers.RecommenderManagerActor.Messages._

  import context.dispatcher

  def newRecommender(): ActorRef = context.actorOf(RecommendationController.props)

  def receive: Receive = {
    case NewRecommenderActor =>

      val recommenderBox = for {
        modelOpt <- contextManagerService.getLatestModel
        recommender = newRecommender()
        _ = modelOpt.foreach(model => recommender ! UpdateContext(model))
      } yield recommender

      recommenderBox pipeTo sender()

  }

}

object RecommenderManagerActor {

  /**
   * Generates recommender manager actor Props in order to create new reference of this actor.
   *
   * @return props for this actor
   */
  def props: Props = Props(new RecommenderManagerActor)

  /**
   * Messages that this actor supports
   */
  object Messages {
    case object NewRecommenderActor
  }

}