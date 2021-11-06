import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val sparkCore = "org.apache.spark" %% "spark-core" % "3.2.0"
  lazy val sparkMLlib = "org.apache.spark" %% "spark-mllib" % "3.2.0"
}
