package scommender
package utils.box

import org.scalatest.{Matchers, WordSpecLike}


class BoxSpec extends WordSpecLike with Matchers {

  case class SomeSpecificException(message: String) extends RuntimeException(message)

  "a box value" should {
    "capture code non fatal exceptions" in {
      val boxValue = Box[Int] {
        throw SomeSpecificException("box spec fault test")
        1
      }

      assert(!boxValue.isSuccessful && boxValue.isInstanceOf[Failed[Int]])
    }

    "deliver successful value" in {
      val boxValue: Box[Int] = Box(2)

      assert(boxValue.isSuccessful && boxValue.isInstanceOf[Successful[Int]])
    }

    "perform map, flatMap, filter methods in for comprehension" in {
      val intBox = Box(2)
      val stringBox = Box("hello")
      val floatBox = Box(12.56F)
      val longBox = Box(12L)

      val boxResult = for {
        _ <- intBox
        _ <- stringBox
        float <- floatBox if float > 1
        long <- longBox
      } yield long

      val failedBox = for {
        _ <- intBox
        _ <- stringBox
        float <- floatBox if float > 34
        long <- longBox
      } yield long

      assert(boxResult == Successful(12L) && failedBox.isInstanceOf[Failed[Long]])
    }
  }

}
