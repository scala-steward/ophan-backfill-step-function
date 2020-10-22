package com.gu.ophan.backfill

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{ AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider }

/**
 * code to get the authentication credentials for google cloud service
 */

object Auth {

  val region = Regions.EU_WEST_1
  val stage = "CODE"

  val awsCredentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider,
    new ProfileCredentialsProvider("ophan"))

  val ssm = AWSSimpleSystemsManagementClientBuilder
    .standard()
    .withRegion(region)
    .withCredentials(awsCredentials)
    .build()

  def getCredentials(env: Env): GoogleCredentials = {
    val parameterName = s"/Ophan/backfill/${env.stage}/google-creds.json"
    val credsString = ssm.getParameter(
      new GetParameterRequest()
        .withWithDecryption(true)
        .withName(parameterName))
      .getParameter
      .getValue
    GoogleCredentials.fromStream(new ByteArrayInputStream(credsString.getBytes()))
  }

}
