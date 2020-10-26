package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import java.time.{ Duration, Instant }
import com.google.cloud.bigquery.JobStatus.State
import com.google.cloud.bigquery.QueryJobConfiguration

/**
 * Step 2: Check if the data extraction job has completed (this check should be repeated periodically until it has completed)
 */

object QueryJobState extends SimpleHandler[JobConfig] {
  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    logger.info(s"checking status of jobid: ${cfg.bqJobId.get}")
    val bq = new BigQuery

    val job = bq.getJob(cfg.bqJobId.get)
    val newState =
      job.getStatus().getState() match {
        case State.DONE => JobState.COMPLETED
        case State.PENDING | State.RUNNING => JobState.RUNNING
      }

    val destTable = job.getConfiguration[QueryJobConfiguration]().getDestinationTable()

    cfg.copy(state = newState, dataTable = Some(destTable.getTable()))
  }
}
