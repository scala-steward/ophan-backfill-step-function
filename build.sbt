import sbtassembly.AssemblyPlugin.autoImport.{assemblyJarName, assemblyMergeStrategy}
import sbtassembly.MergeStrategy

name := "ophan-backfill"

organization := "com.gu"

description:= "ophan backfill step function lambdas"

version := "1.0"

scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.2.0",
  "com.amazonaws" % "aws-java-sdk-ssm" % "1.11.883",
  "com.lihaoyi" %% "upickle" % "1.2.2",
  "com.google.cloud" % "google-cloud-bigquery" % "1.122.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
  "org.slf4j" % "slf4j-api" % "1.7.30"
)

enablePlugins(RiffRaffArtifact)

assemblyJarName := s"${name.value}.jar"
assemblyMergeStrategy in assembly := {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat" => new MergeLog4j2PluginCachesStrategy
  case _ => MergeStrategy.first
}
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), "cloudformation/cfn.yaml")
