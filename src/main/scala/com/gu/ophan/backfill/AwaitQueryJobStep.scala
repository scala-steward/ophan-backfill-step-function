package com.gu.ophan.backfill

import com.google.cloud.bigquery.QueryJobConfiguration
import com.google.cloud.bigquery.Job

/**
 * Step 2: Check if the data collection (query) job has completed
 * (this check should be repeated periodically until it has completed)
 */

object AwaitQueryJobStep extends SimpleHandler[JobConfig] with AwaitJob {
  override def onComplete(cfg: JobConfig, job: Job): JobConfig = {
    val destTable = Option(job.getConfiguration[QueryJobConfiguration]().getDestinationTable)
    logger.info(s"Destination table: $destTable")
    cfg.copy(dataTable = destTable.map(dt => (dt.getDataset, dt.getTable)))
  }
}
