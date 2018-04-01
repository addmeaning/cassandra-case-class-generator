name := "cassandra-case-class-generator"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "com.outworkers" % "phantom-dsl_2.11" % "2.20.2",
  "com.eed3si9n" %% "treehugger" % "0.4.3",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)


resolvers += Resolver.sonatypeRepo("public")