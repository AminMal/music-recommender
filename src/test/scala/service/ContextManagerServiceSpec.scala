package ir.ac.usc
package service

import controllers.ContextManagerActor.Responses.{CMOperationResult, OperationFailure, SuccessfulOperation}
import models.{SongDTO, User}
import org.scalatest.{AsyncWordSpec, Matchers}

class ContextManagerServiceSpec extends AsyncWordSpec with Matchers {

  val provider = new ServiceProvider("context-manager-service")
  import provider._
  val dummyUser: User = User(
    1, None, "none"
  )
  val dummySong: SongDTO = SongDTO(
    0L, "some song", 12L, "12|23", "dummy artist", 13
  )

  "context manager service" should {
    "add user" in {
      service.contextManagerService.addUser(dummyUser).map { result =>
        assert(result == SuccessfulOperation || result.isInstanceOf[OperationFailure])
      }
    }

    "add song" in {
      service.contextManagerService.addSong(dummySong).map { result =>
        assert(result == SuccessfulOperation || result.isInstanceOf[OperationFailure])
      }
    }

    "add user rating" in {
      service.contextManagerService.addUserRating(dummyUser.userId.toLong, dummySong.id, 1.0)
        .map { result =>
          assert(result == SuccessfulOperation || result.isInstanceOf[OperationFailure])
        }
    }
  }

}
