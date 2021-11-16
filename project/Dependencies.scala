import sbt._

object Dependencies {
  val AkkaVersion            = "2.6.8"
  val AkkaHttpVersion        = "10.2.7"

  // ------- Scala test libs -------
  lazy val ScalaTest         = "org.scalatest" %% "scalatest" % "3.0.5"
  // ------- Spark libs ------------
  lazy val SparkCore         = "org.apache.spark" %% "spark-core" % "3.2.0"
  lazy val SparkMLlib        = "org.apache.spark" %% "spark-mllib" % "3.2.0"
  // ------- Akka libs -------------
  lazy val AkkaActorsTyped   = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  lazy val AkkaStreams       = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  lazy val AkkaHttp          = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  lazy val SprayJson         = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
}
