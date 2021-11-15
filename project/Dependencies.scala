import sbt._

object Dependencies {
  val AkkaVersion            = "2.6.8"
  val AkkaHttpVersion        = "10.2.7"

  // ------- Scala test libs -------
  lazy val scalaTest         = "org.scalatest" %% "scalatest" % "3.0.5"
  // ------- Spark libs ------------
  lazy val sparkCore         = "org.apache.spark" %% "spark-core" % "3.2.0"
  lazy val sparkMLlib        = "org.apache.spark" %% "spark-mllib" % "3.2.0"
  // ------- Akka libs -------------
  lazy val AkkaActorsTyped   = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  lazy val AkkaStreams       = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  lazy val AkkaHttp          = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
}
