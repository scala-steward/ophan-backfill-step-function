package com.gu.ophan.backfill.cfn

import scala.jdk.CollectionConverters._

import software.amazon.awscdk.core.{ App => CdkApp, _ }
import software.amazon.awscdk.services.iam._

class CloudformationStack(scope: Construct, id: String)
    extends Stack(scope, id)
    with CdkHelpers {

  param("BuildId")()
  param("Stack")(
    _.description("Stack name")
      .defaultValue("ophan-backfill")
  )
  param("App")(
    _.description("Application name")
      .defaultValue("ophan-backfill")
  )
  val stageParam = param("Stage")(
    _.description("Stage name")
      .defaultValue("CODE")
      .withAllowedValues("CODE", "PROD")
  )
  param("DeployBucket")(
    _.description("Bucket where RiffRaff uploads artifacts on deploy")
      .defaultValue("ophan-dist")
  )

  val executionRole = Role.Builder.create(this, "ExecutionRole")
    .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
    .path("/")
    .withInlinePolicies(
      "logs" -> policyDocument(
        policyStatement(
            actions = List(
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents"),
            resources = List(
              "arn:aws:logs:*:*:*"))),
      "lambda" -> policyDocument(
        policyStatement(
          actions = List("lambda:InvokeFunction"),
          resources = List("*"))),
      "params" -> policyDocument(
        policyStatement(
          actions = List("ssm:GetParameter"),
          resources = List(
            s"arn:aws:ssm:eu-west-1:021353022223:parameter/Ophan/backfill/${stageParam.getValueAsString()}/google-creds.json"))))
    .build()

  val statesExecutionRole = Role.Builder.create(this, "StatesExecutionRole")
    .assumedBy(new ServicePrincipal(s"states.${getRegion()}.amazonaws.com"))
    .path("/")
    .withInlinePolicies(
      "StatesExecutionPolicy" -> policyDocument(
        policyStatement(actions = List("lambda:InvokeFunction"),
          resources = List("*"))))
   .build()
}

object CloudformationApp {
  def main(args: Array[String]): Unit = {
    val app = new CdkApp()
    new CloudformationStack(app, "ophan-backfill")
    app.synth()
  }
}
