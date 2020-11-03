package com.gu.ophan.backfill.cfn

import software.constructs.Construct
import software.amazon.awscdk.services.stepfunctions.{ Map => MapStep, _ }
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke

import CdkHelpers._

class BackfillStates(scope: Construct, lambdas: BackfillLambdas) {

  val pauseForQueryJob =
    Wait.Builder.create(scope, "PauseForQueryJobToRun")

  val addExecutionId =
    Pass.Builder.create(scope, "addExecutionId")
      .parameters(
        "startDateInc.$" -> "$.startDateInc",
        "endDateExc.$" -> "$.endDateExc",
        "executionId.$" -> "$$.Execution.Name"
      ).build()

  val initJobTask =
    LambdaInvoke.Builder.create(scope, "InitJobState")
      .lambdaFunction(lambdas.stepInitJob)
      .build()

  val partitionTask =
    LambdaInvoke.Builder.create(scope, "PartitionState")
      .lambdaFunction(lambdas.stepPartitionTimespan)
      .build()

  val mapOverPartitionTask =
    MapStep.Builder.create(scope, "MapOverPartition")
      .inputPath("$")
      .itemsPath("$")
      .maxConcurrency(2)
      .resultPath("$")

  val stateChain =
    Chain.start(addExecutionId)
      .next(initJobTask)

  val stateMachine = StateMachine.Builder.create(scope, "Ophan-Backfill-Extractor")
    .definition(stateChain)
    .build()

}
