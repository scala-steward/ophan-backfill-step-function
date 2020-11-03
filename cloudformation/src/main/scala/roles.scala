package com.gu.ophan.backfill.cfn

import software.amazon.awscdk.services.iam.{
  PolicyDocument, PolicyStatement, Role, ServicePrincipal
}

import scala.jdk.CollectionConverters._
import CdkHelpers._
import software.constructs.Construct

class BackfillRoles(
  scope: Construct,
  params: BackfillParams,
  region: String
) {
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
          .resources(params.bigQueryAuthParamArn)
          .build()
      ).build(),
  ).asJava

  val executionRole = Role.Builder.create(scope, "ExecutionRole")
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

  val statesExecutionRole = Role.Builder.create(scope, "StatesExecutionRole")
    .assumedBy(new ServicePrincipal(s"states.${region}.amazonaws.com"))
    .path("/")
    .inlinePolicies(statesExecutionPolicies)
    .build()

}
