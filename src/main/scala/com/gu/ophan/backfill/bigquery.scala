package com.gu.ophan.backfill

import com.google.cloud.bigquery.{ Option => _, _ }
import com.google.api.gax.paging.Page
import com.google.cloud.bigquery.QueryJobConfiguration
import java.time.Instant

class BigQuery(implicit env: Env) {
  val projectId = "datatech-platform-" + env.stage.toLowerCase
  val tableId = "pageviews"

  protected lazy val client =
    BigQueryOptions.newBuilder()
      .setCredentials(Auth.getCredentials(env))
      .setLocation("europe-west2")
      .build()
      .getService()

  // creates a new query job and returns the details of the job
  def query(querySrc: String, jobId: Option[String] = None, dryRun: Boolean = true): JobInfo = {
    val jobCfg = QueryJobConfiguration
      .newBuilder(querySrc)
      .setDryRun(dryRun)
      .build()

    val jobInfo = jobId match {
      case Some(id) => JobInfo.of(JobId.of(id, projectId), jobCfg)
      case None => JobInfo.of(jobCfg)
    }

    client.create(jobInfo)
  }

  def getJob(jobId: String): Job = client.getJob(jobId)

}
