package com.gu.ophan.backfill.cfn

import software.amazon.awscdk.core.CfnParameter
import software.amazon.awscdk.core.Stack

import CdkHelpers._

class BackfillParams(scope: Stack) {
  lazy val bigQueryAuthParamArn =
    s"arn:aws:ssm:eu-west-1:021353022223:parameter/Ophan/backfill/${stageParam.getValueAsString()}/google-creds.json"

  CfnParameter.Builder.create(scope, "BuildId")
    .build()

  val stackParam = CfnParameter.Builder.create(scope, "Stack")
    .description("Stack name")
    .defaultValue("ophan")
    .build()

  val appParam = CfnParameter.Builder.create(scope, "App")
    .description("Application name")
    .defaultValue("ophan-backfill")
    .build()

  val stageParam = CfnParameter.Builder.create(scope, "Stage")
    .description("Stage name")
    .allowedValues("CODE", "PROD")
    .defaultValue("CODE")
    .build()

  val deployBucketParam = CfnParameter.Builder.create(scope, "DeployBucket")
    .description("Bucket where RiffRaff uploads artifacts on deploy")
    .defaultValue("ophan-dist")
    .build()
}
