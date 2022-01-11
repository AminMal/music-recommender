package scommender
package utils.box

import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.Future

class BoxFSpec extends AsyncWordSpecLike with Matchers with BoxSupport {

  "a boxF value" should {
    "perform map, flatMap, filter methods in for comprehensions" in {
      val boxfResult = for {
        _ <- toBoxF(Future.successful(2))
        _ <- toBoxF(Future.successful("some string"))
        floatFiltered <- toBoxF(Future.successful(12.56F)) if floatFiltered > 12.0F
        long <- toBoxF(Future.successful(12L))
      } yield long

      assert(boxfResult.underlying.isSuccessful)
      boxfResult.underlying.asInstanceOf[Successful[Future[Long]]].value
        .map(value => assert(value == 12L))

    }
  }

}
