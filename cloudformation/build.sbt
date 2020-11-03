organization := "com.gu"
name := "backfill-cloudformation"
version := "0.1"

scalaVersion := "2.13.3"

val cdkVersion = "1.71.0"

libraryDependencies ++= Seq(
  "software.amazon.awscdk" % "core" % cdkVersion,
  "software.amazon.awscdk" % "iam" % cdkVersion,
  "software.amazon.awscdk" % "lambda" % cdkVersion,
  "software.amazon.awscdk" % "stepfunctions-tasks" % cdkVersion,
)
