package scommender

import utils.box.{BoxF, Failed, Successful}
import org.scalactic.source
import org.scalactic.source.Position
import org.scalatest.{Assertion, AsyncWordSpec}

trait BoxFWordSpecLike extends AsyncWordSpec {

  trait InBoxWordProvider {
    val leftString: String
    val pos: source.Position

    def inBox(assertion: BoxF[Assertion]): Unit = {
      leftString in {
        assertion.underlying match {
          case Successful(value) => value
          case Failed(cause) => throw cause
        }
      }
    }
  }

  implicit def strToInBoxProvider(s: String)(implicit pos: source.Position): InBoxWordProvider = new InBoxWordProvider {
    override val leftString: String = s
    override val pos: Position = pos
  }

}
