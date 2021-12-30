package ir.ac.usc
package utils

import evaluation.MetricsEnum
import evaluation.MetricsEnum.MetricsEnum
import exception.EntityNotFoundException

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}


/**
 * Custom path matchers are stored here.
 */
object Matchers {

  object EvaluationMethod extends PathMatcher1[MetricsEnum] {
    override def apply(path: Path): PathMatcher.Matching[Tuple1[MetricsEnum]] = path match {
      case Path.Segment(methodName, tail) =>
        val methodOpt = MetricsEnum.withNameOpt(methodName)
        if (methodOpt.isEmpty)
          throw EntityNotFoundException("evaluation metric")

        Matched(tail, Tuple1(methodOpt.get))

      case _ => Unmatched
    }
  }

}
