package com.gu.ophan.backfill.cfn

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

  val params = new BackfillParams(this)
  val roles = new BackfillRoles(this, params, getRegion())
  val lambdas = new BackfillLambdas(this, params, roles)
  val states = new BackfillStates(this, lambdas)
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
