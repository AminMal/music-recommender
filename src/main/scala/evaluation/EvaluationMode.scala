package ir.ac.usc
package evaluation

import scala.util.Try

/**
 * Evaluation mode is an enumeration object that indicates what to do with the resulting dataframe
 * in the performance evaluator actor.
 */
object EvaluationMode extends Enumeration {

  type EvaluationMode = Value

  val FireAndForget: EvaluationMode = Value("fire and forget")
  val Wait: EvaluationMode = Value("wait")

  /**
   * returns an optional EvaluationMethod based on the given name, if found.
   *
   * @param name name of the mode
   * @return Optional mode
   */
  def withNameOpt(name: String): Option[EvaluationMode] = Try(super.withName(name)).toOption

}
