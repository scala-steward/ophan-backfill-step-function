package com.gu.ophan.backfill.cfn

import com.softwaremill.macwire._

import scala.jdk.CollectionConverters._

import software.amazon.awscdk.core.{ App => CdkApp, _ }
import software.amazon.awscdk.services.lambda.{ SingletonFunction, Code, Runtime }
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke
import software.amazon.awscdk.services.stepfunctions.{ Map => MapStep, _ }

import CdkHelpers._

class CloudformationStack(scope: Construct, id: String, props: StackProps)
    extends Stack(scope, id, props) {

  lazy val stack = this

  lazy val params = wire[BackfillParams]
  lazy val roles = wire[BackfillRoles]
  lazy val lambdas = wire[BackfillLambdas]
  lazy val states = wire[BackfillStates]

  StateMachine.Builder.create(stack, "Ophan-Backfill-Extractor")
    .definition(states.initialSteps)
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

    val stackName = "ophan-backfill-PROD"

    new CloudformationStack(app, stackName, props)
    app.synth()
  }
}
