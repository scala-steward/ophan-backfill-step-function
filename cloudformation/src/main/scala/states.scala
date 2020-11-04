package com.gu.ophan.backfill.cfn

import software.constructs.Construct
import software.amazon.awscdk.services.stepfunctions.{ Map => MapStep, _ }
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.lambda.IFunction

import CdkHelpers._
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.Stack


class BackfillStates(scope: Stack, lambdas: BackfillLambdas) {

  lazy val errorInJob = Fail.Builder.create(scope, "ErrorInJob")
    .cause("$.error")
    .build()

  /**
    * WaitFor defines the following construct:
    *    +-------------+
    *    |[initialiser]|     < sets up the job that
    *    +-------------+         is to be waited for >
    *          |
    *          |
    *       +-----+
    *  ____ |pause|
    * /     +-----+
    * |        |
    * |        |
    * |        |
    * |   +---------+
    * |   |[checker]|  < assesses whether the job has finished
    * |   +---------+          or not and sets status value >
    * |        |
    * |        |                    yes           +-----------+
    * |   is state complete? ---------------------|[nextState]|
    * |      /          \                         +-----------+
    * |     /  no        \                 +------------+
    * \____/              \_______________ |[errorState]|
    *                           error      +------------+
    */

  def WaitFor(
    name: String,
    initialiser: IChainable,
    checker: IChainable,
    nextState: IChainable,
    errorState: IChainable = errorInJob,
    waitTime: WaitTime = WaitTime.duration(Duration.minutes(1))
  ) = {

    val pauseState = Wait.Builder.create(scope, s"PauseFor-$name")
      .time(waitTime)
      .build()

    Chain.start(initialiser)
      .next(pauseState)
      .next(checker)
      .next(Choice.Builder.create(scope, name)
        .build()
        .when(Condition.stringEquals("$.state", "RUNNING"), pauseState)
        .when(Condition.stringEquals("$.state", "WAITING"), nextState)
        .when(Condition.stringEquals("$.state", "ERROR"), errorState))
  }

  def LambdaTask(name: String, lambda: IFunction) =
    LambdaInvoke.Builder.create(scope, name)
      .lambdaFunction(lambda)
      .build()

  // in order to stash the resulting data into a bucket path that is
  // prefixed with the excution ID, we need to grab it from the Step
  // function's Context, as it is a attribute of the execution of the
  // step function rather than an individual lambda. This simple steps
  // achieves that by using a pass-through which modifies the data on
  // the way out to add the execution id.

  val addExecutionId =
    Pass.Builder.create(scope, "addExecutionId")
      .parameters(
        "startDateInc.$" -> "$.startDateInc",  // \____ retain these two parameters
        "endDateExc.$" -> "$.endDateExc",      // /
        "executionId.$" -> "$$.Execution.Name" // `$$` is the context object for the step function
      ).build()

  val partitionTask = LambdaTask("PartitionState", lambdas.stepPartitionTimespan)

  // this is the set of states that is applied to each partition of
  // the time frame being extracted.
  lazy val queryBigQuery =
    WaitFor(name = "WaitForInitJob",
      initialiser = LambdaTask("InitJobTask", lambdas.stepInitJob),
      checker = LambdaTask("CheckQueryJobStatus", lambdas.stepQueryJobState),
      nextState = extractData)

  lazy val extractData =
    WaitFor(name = "WaitForExtractDataJob",
      initialiser = LambdaTask("ExtractDataStartJob", lambdas.stepExtractData),
      checker = LambdaTask("CheckExtractJobStatus", lambdas.stepAwaitExtractJob),
      nextState = Succeed.Builder.create(scope, "DataExtractedSuccessfully")
        .build()
    )

  lazy val mapOverPartitionTask =
    MapStep.Builder.create(scope, "MapOverPartition")
      .inputPath("$")
      .itemsPath("$")
      .maxConcurrency(2)
      .resultPath("$")
      .build()
      .iterator(queryBigQuery)

  lazy val initialSteps =
    Chain.start(addExecutionId)
      .next(partitionTask)
      .next(mapOverPartitionTask)

}
