package com.gu.ophan.backfill.cfn

import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.Stack

import java.util.UUID
import CdkHelpers._

class BackfillLambdas(
  scope: Stack,
  params: BackfillParams,
  roles: BackfillRoles
) {
  val lambdaCodeBucket =
    Code.fromBucket(
      Bucket.fromBucketName(scope, "lambdaCodeBucket",
        params.deployBucketParam.getValueAsString()),
      s"ophan/${params.stageParam.getValueAsString()}/ophan-backfill/ophan-backfill.jar")


  // the individual lambdas which make up the "Task"-type steps of the
  // Step function -- there's a lot of common code here so defining a
  // def for reuse

  def stepLambda(name: String,
    handler: String,
    memorySize: Int = 1024,
    description: String = "ophan backfill",
    timeout: Duration = Duration.minutes(1)
  ) =
    Function.Builder.create(scope, name)
      .functionName(s"${scope.getStackName()}-$name-${params.stageParam.getValueAsString()}")
      .code(lambdaCodeBucket)
      .handler(handler)
      .runtime(Runtime.JAVA_8)
      .role(roles.executionRole)
      .environment(
        "Stage" -> params.stageParam.getValueAsString(),
        "Stack" -> params.stackParam.getValueAsString(),
        "App" -> params.appParam.getValueAsString())
      .memorySize(memorySize)
      .timeout(timeout)
      .build()

  val stepInitJob = stepLambda(name = "InitJobLambda",
    description = "initiate the big query job",
    handler = "com.gu.ophan.backfill.InitBackfillStep::handleRequest")

  val stepQueryJobState = stepLambda(name = "QueryJobStateLambda",
    description = "query the big query job and wait for it to finish",
    handler = "com.gu.ophan.backfill.AwaitQueryJobStep::handleRequest")

  val stepExtractData = stepLambda(name = "ExtractDataLambda",
    description = "initiate bigquery extract job",
    handler = "com.gu.ophan.backfill.ExtractDataStep::handleRequest")

  val stepAwaitExtractJob = stepLambda(name = "AwaitExtractJobLambda",
    description = "wait for big query extraction job to complete",
    handler = "com.gu.ophan.backfill.AwaitExtractJobStep::handleRequest")

  val stepPartitionTimespan = stepLambda(name = "PartitionStepLambda",
    description = "split the job timespan into smaller jobs to make the data easier to find in GCS",
    handler = "com.gu.ophan.backfill.PartitionStep::handleRequest")

}
