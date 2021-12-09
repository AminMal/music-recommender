package ir.ac.usc
package controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import models.{SongDTO, User}

import akka.testkit.{ImplicitSender, TestKit}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ContextManagerActorSpec extends TestKit(ActorSystem("context-manager"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import ContextManagerActor.Messages._
  import ContextManagerActor.Responses._
  val contextManagerActor: ActorRef = system.actorOf(Props[ContextManagerActor])

  val mockUser: User = User(
    userId = 0,
    cityId = Some(12),
    gender = "female"
  )

  val mockSong: SongDTO = SongDTO(
    id = 12L,
    name = "Some song in test spec",
    artistName = "Scommender",
    length = 67232L,
    genreIds = "12|32",
    language = 12D
  )

  "a context manager actor" should {
    "return optional matrix factorization model" in {
      contextManagerActor ! GetLatestModel
      expectMsgType[Option[MatrixFactorizationModel]]
    }

    "add user to spark users df" in {
      contextManagerActor ! AddUser(mockUser)
      expectMsgType[CMOperationResult]
    }

    "add song to spark songs df" in {
      contextManagerActor ! AddSong(mockSong)
      expectMsgType[CMOperationResult]
    }

    "add user rating in spark ratings df" in {
      contextManagerActor ! AddUserRating(mockUser.userId.toLong, mockSong.id, 1D)
      expectMsgType[CMOperationResult]
    }
  }
}
