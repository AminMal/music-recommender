package ir.ac.usc
package evaluation

import scala.util.Try

object EvaluationMode extends Enumeration {

  type EvaluationMode = Value

  val FireAndForget: EvaluationMode = Value("fire and forget")
  val Wait: EvaluationMode          = Value("wait")

  def withNameOpt(name: String): Option[EvaluationMode] = Try(super.withName(name)).toOption

}
