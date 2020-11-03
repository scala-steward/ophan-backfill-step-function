package com.gu.ophan.backfill.cfn

import scala.jdk.CollectionConverters._

import software.amazon.awscdk.core.{ App => CdkApp, _ }
import software.amazon.awscdk.services.iam._

class CloudformationStack(scope: Construct, id: String, props: StackProps)
    extends Stack(scope, id, props)
    with CdkHelpers {

  lazy val bigQueryAuthParamArn =
    s"arn:aws:ssm:eu-west-1:021353022223:parameter/Ophan/backfill/${stageParam.getValueAsString()}/google-creds.json"

  CfnParameter.Builder.create(this, "BuildId")
    .build()

  CfnParameter.Builder.create(this, "Stack")
    .description("Stack name")
    .defaultValue("ophan")
    .build()

  CfnParameter.Builder.create(this, "App")
    .description("Application name")
    .defaultValue("ophan-backfill")
    .build()

  val stageParam = CfnParameter.Builder.create(this, "Stage")
    .description("Stage name")
    .allowedValues("CODE", "PROD")
    .defaultValue("CODE")
    .build()

  CfnParameter.Builder.create(this, "DeployBucket")
    .description("Bucket where RiffRaff uploads artifacts on deploy")
    .defaultValue("ophan-dist")
    .build()

  val executionRolePolicies = Map(
    "logs" -> PolicyDocument.Builder.create()
      .statements(
        PolicyStatement.Builder.create()
          .actions(
            "logs:CreateLogGroup",
            "logs:CreateLogStream",
            "logs:PutLogEvents")
          .resources("arn:aws:logs:*:*:*")
          .build()
      ).build(),
    "lambda" -> PolicyDocument.Builder.create()
      .statements(
        PolicyStatement.Builder.create()
          .actions("lambda:InvokeFunction")
          .resources("*")
          .build()
      ).build(),
    "params" -> PolicyDocument.Builder.create()
      .statements(
        PolicyStatement.Builder.create()
          .actions("ssm:GetParameter")
          .resources(bigQueryAuthParamArn)
          .build()
      ).build(),
  ).asJava

  val executionRole = Role.Builder.create(this, "ExecutionRole")
    .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
    .path("/")
    .inlinePolicies(executionRolePolicies)
    .build()

  val statesExecutionPolicies = Map(
    "StatesExecutionPolicy" -> PolicyDocument.Builder.create()
      .statements(PolicyStatement.Builder.create()
        .actions("lambda:InvokeFunction")
        .resources("*")
        .build()
      ).build()
  ).asJava

  val statesExecutionRole = Role.Builder.create(this, "StatesExecutionRole")
    .assumedBy(new ServicePrincipal(s"states.${getRegion()}.amazonaws.com"))
    .path("/")
    .inlinePolicies(statesExecutionPolicies)
    .build()
}

object CloudformationApp {
  def main(args: Array[String]): Unit = {
    val app = new CdkApp()
    val props = StackProps.builder()
      .env(Environment.builder()
        .account("021353022223")
        .region("eu-west-1")
        .build()
      ).build()

    new CloudformationStack(app, "ophan-backfill-PROD", props)
    app.synth()
  }
}
