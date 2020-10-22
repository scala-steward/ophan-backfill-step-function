package com.gu.ophan.backfill

import com.google.cloud.bigquery.{ Option => _, _ }
import com.google.api.gax.paging.Page
import com.google.cloud.bigquery.QueryJobConfiguration
import java.time.Instant

object helpers {
  import scala.jdk.CollectionConverters._

  implicit class PageHelper[T](page: Page[T]) {
    def toList: List[T] = page.iterateAll().asScala.toList
  }
}

class BigQuery(env: Env) {
  import helpers._

  val projectId = "datatech-platform-" + env.stage.toLowerCase
  val datasetId = DatasetId.of(projectId, "public")
  val tableId = "pageviews"

  lazy val client =
    BigQueryOptions.newBuilder()
      .setCredentials(Auth.getCredentials(env))
      .setLocation("europe-west2")
      .build()
      .getService()

  // creates a new query job and returns the details of the job
  def query(querySrc: String, jobId: Option[String] = None, dryRun: Boolean = true): String = {
    val jobCfg = QueryJobConfiguration
      .newBuilder(querySrc)
      .setDryRun(dryRun)
      .build()

    val jobInfo = jobId match {
      case Some(id) => JobInfo.of(JobId.of(id, projectId), jobCfg)
      case None => JobInfo.of(jobCfg)
    }

    // DEBUG - returing a fake JobID for testing purposes until we get
    // access to create real jobs.

    s"fake_jobid_" + Instant.now().toString

    //    client.create(jobInfo)

  }

}
