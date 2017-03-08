import sbt.Keys._
import sbt._
import sbtrelease.Version

name := "assignment-sender-service"

scalaVersion := "2.11.8"
releaseNextVersion := { ver => Version(ver).map(_.bumpMinor.string).getOrElse("Error") }
assemblyJarName in assembly := "assignment-sender-service.jar"
cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)
offline := true
parallelExecution := false

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-core" % "1.11.99",
  "com.amazonaws" % "aws-java-sdk-ses" % "1.11.99",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.99",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.99",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0" intransitive(),
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "org.zeroturnaround" % "zt-zip" % "1.11",
  "org.slf4j" % "slf4j-log4j12" % "1.7.24",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.7.14" % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")
