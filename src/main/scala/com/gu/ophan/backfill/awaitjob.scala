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
    val nextCfg =
      jobStatus.getState() match {
        case State.DONE if jobStatus.getError() != null =>
          logger.error(s"Job completed with error ${jobStatus.getError()}")
          cfg.copy(state = JobState.ERROR, errorMsg = Some(jobStatus.getError().toString()))
        case State.DONE =>
          cfg.copy(state = JobState.WAITING)
        case State.PENDING | State.RUNNING =>
          cfg.copy(state = JobState.RUNNING)
      }

    onComplete(nextCfg, job)
  }

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = awaitBigQueryJob(cfg)

}
