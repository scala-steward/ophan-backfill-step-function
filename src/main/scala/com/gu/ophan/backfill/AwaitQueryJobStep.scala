package com.gu.ophan.backfill

import com.google.cloud.bigquery.QueryJobConfiguration
import com.google.cloud.bigquery.Job

/**
 * Step 2: Check if the data collection (query) job has completed
 * (this check should be repeated periodically until it has completed)
 */

object AwaitQueryJobStep extends SimpleHandler[JobConfig] with AwaitJob {
  override def onComplete(cfg: JobConfig, job: Job): JobConfig = {
    val destTable = job.getConfiguration[QueryJobConfiguration]().getDestinationTable()

    if(destTable == null) {
      val msg = "Query job completed, but there was not destination table (destTable == null)"
      logger.error(msg)
      cfg.copy(state = JobState.ERROR, errorMsg = Some(msg))
    } else {
      logger.info(s"Destination table: ${destTable}")
      cfg.copy(dataTable = Some((destTable.getDataset(), destTable.getTable())))
    }
  }
}
