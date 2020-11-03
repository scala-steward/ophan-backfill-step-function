package com.gu.ophan.backfill.cfn

import scala.jdk.CollectionConverters._
import software.amazon.awscdk.core.CfnParameter
import software.constructs.Construct
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.PolicyDocument
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.stepfunctions.Pass

object CdkHelpers {
  implicit class CfnParameterHelper(bld: CfnParameter.Builder) {
    def allowedValues(values: String*): CfnParameter.Builder =
      bld.allowedValues(values.asJava)
  }

  implicit class RoleHelper(bld: Role.Builder) {
    def inlinePolicies(policies: (String, PolicyDocument)*) =
      bld.inlinePolicies(policies.toMap.asJava)
  }

  implicit class PolicyDocumentHelper(bld: PolicyDocument.Builder) {
    def statements(stmts: PolicyStatement*) = bld.statements(stmts.asJava)
  }

  implicit class PolicyStatementHelper(bld: PolicyStatement.Builder) {
    def actions(args: String*) = bld.actions(args.asJava)
    def resources(args: String*) = bld.resources(args.asJava)
  }

  implicit class FunctionHelper(bld: Function.Builder) {
    def environment(args: (String, String)*) = bld.environment(args.toMap.asJava)
  }

  implicit class PassHelper(bld: Pass.Builder) {
    def parameters(args: (String, String)*) = bld.parameters(args.toMap.asJava)
  }
}
