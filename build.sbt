name := "ophan-backfill"

organization := "com.gu"

description:= "ophan backfill step function lambdas"

version := "1.0"

scalaVersion := "2.13.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code",
  "-Wunused:imports"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",

  "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.36", //  log4j-over-slf4j provides `org.apache.log4j.MDC`, which is dynamically loaded by the Lambda runtime
  "ch.qos.logback" % "logback-classic" % "1.2.10",

  "com.amazonaws" % "aws-java-sdk-ssm" % "1.12.259",
  "com.lihaoyi" %% "upickle" % "1.4.3",
  "com.google.cloud" % "google-cloud-bigquery" % "2.5.1",
  "com.google.cloud" % "google-cloud-storage" % "2.2.3",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test
)

enablePlugins(RiffRaffArtifact, BuildInfoPlugin)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cloudformation/cloudformation.yaml"), "cloudformation/cfn.yaml")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

buildInfoPackage := "com.gu.ophan.backfill"
buildInfoKeys := {
  val buildInfo = com.gu.riffraff.artifact.BuildInfo(baseDirectory.value)
  Seq[BuildInfoKey](
    "buildNumber" -> buildInfo.buildIdentifier,
    "gitCommitId" -> buildInfo.revision,
    "buildTime" -> System.currentTimeMillis
  )
}
