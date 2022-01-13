organization := "com.gu"
name := "backfill-cloudformation"
version := "0.1"

scalaVersion := "2.13.8"

val cdkVersion = "1.71.0"

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.5.0" % "provided", // forgive me ;-)
  "software.amazon.awscdk" % "core" % cdkVersion,
  "software.amazon.awscdk" % "iam" % cdkVersion,
  "software.amazon.awscdk" % "lambda" % cdkVersion,
  "software.amazon.awscdk" % "stepfunctions-tasks" % cdkVersion,
)
