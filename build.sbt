import Dependencies._

ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "securitybuilderexample",
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    libraryDependencies += "com.tersesystems.securitybuilder" % "securitybuilder" % "1.0.0",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.9.0",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
