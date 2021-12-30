package scommender
package service

import models.{SongDTO, User}

import akka.Done
import org.scalatest.Matchers

class ContextManagerServiceSpec extends BoxFWordSpecLike with Matchers {

  val provider = new ServiceProvider("context-manager-service")
  import provider._
  val dummyUser: User = User(
    1, None, "none"
  )
  val dummySong: SongDTO = SongDTO(
    0L, "some song", 12L, "12|23", "dummy artist", 13
  )

  "context manager service" should {
    "add user" inBox {
      service.contextManagerService.addUser(dummyUser)
        .map(result => assert(result == Done))
    }

    "add song" inBox {
      service.contextManagerService.addSong(dummySong).map { result =>
        assert(result == Done)
      }
    }

    "add user rating" inBox {
      service.contextManagerService.addUserRating(dummyUser.userId.toLong, dummySong.id, 1.0)
        .map { result =>
          assert(result == Done)
        }
    }
  }

}
