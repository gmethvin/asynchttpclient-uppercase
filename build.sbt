
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.8.16",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.13"
)

fork in run := true

