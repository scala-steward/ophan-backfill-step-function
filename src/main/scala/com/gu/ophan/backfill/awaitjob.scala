package com.gu.ophan.backfill

import com.google.cloud.bigquery.JobStatus.State
import com.google.cloud.bigquery.Job

trait AwaitJob extends SimpleHandler[JobConfig] {

  def onComplete(cfg: JobConfig, job: Job): JobConfig = cfg

  def awaitBigQueryJob(cfg: JobConfig)(implicit env: Env): JobConfig = {
    logger.info(s"checking status of jobid: ${cfg.bqJobId.get}")
    val bq = new BigQuery

    val job = {
      val baseJob = bq.getJob(cfg.bqJobId.get)
      if (cfg.async) baseJob else baseJob.waitFor().reload()
    }

    val jobStatus = job.getStatus()
    val newState =
      jobStatus.getState() match {
        case State.DONE =>
          val err = jobStatus.getError()
          assert(err == null, s"Job failed: $err")
          JobState.WAITING
        case State.PENDING | State.RUNNING => JobState.RUNNING
      }

    onComplete(cfg.copy(state = newState), job)
  }

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = awaitBigQueryJob(cfg)

}
