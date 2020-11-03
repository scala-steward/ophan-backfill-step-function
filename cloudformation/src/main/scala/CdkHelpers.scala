package com.gu.ophan.backfill.cfn

import scala.jdk.CollectionConverters._
import software.amazon.awscdk.core.CfnParameter
import software.constructs.Construct
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.PolicyDocument
import software.amazon.awscdk.services.iam.PolicyStatement

trait CdkHelpers extends Construct {

//  implicit def autoBuild[T](bld: { def build(): T }): T = bld.build()

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

  // def policyDocument(stmts: PolicyStatement*) =
  //   PolicyDocument.Builder.create().statements(stmts.asJava).build()

  // def policyStatement(actions: Seq[String], resources: Seq[String]) =
  //   PolicyStatement.Builder.create()
  //     .actions(actions.asJava)
  //     .resources(resources.asJava)
  //     .build()

  // def param(name: String, `type`: String = "String")
  //   (op: CfnParameter.Builder => CfnParameter.Builder = identity) =
  //   op(CfnParameter.Builder.create(this, name))
  //     .`type`(`type`)
  //     .build()

}
