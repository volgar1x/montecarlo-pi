scalaVersion := "2.11.4"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9"
)
