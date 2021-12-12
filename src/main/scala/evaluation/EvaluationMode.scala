package ir.ac.usc
package evaluation

object EvaluationMode extends Enumeration {

  type EvaluationMode = Value

  val FireAndForget: EvaluationMode = Value("fire and forget")
  val Wait: EvaluationMode          = Value("wait")

}
