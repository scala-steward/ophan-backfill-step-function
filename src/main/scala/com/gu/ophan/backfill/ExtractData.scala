package com.gu.ophan.backfill

import java.time.{ Instant, LocalDate, ZoneId }
import java.time.format.DateTimeFormatter

/**
 * Step 3: Extract data from temporary table created during job
 */

object ExtractDataStep extends SimpleHandler[JobConfig] {

  def fileNameFormat(startDateInc: LocalDate, endDateExc: LocalDate) = s"${startDateInc}_${endDateExc.toEpochDay - startDateInc.toEpochDay}.*.csv"

  def datestamp(cfg: JobConfig) =
    s"${cfg.startDateInc}--${cfg.endDateExc}"

  def destinationURI(cfg: JobConfig)(implicit env: Env) =
    s"gs://gu-ophan-backfill-${env.stage.toLowerCase}/${cfg.executionId}/${fileNameFormat(cfg.startDateInc, cfg.endDateExc)}"

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    val bq = new BigQuery
    val (datasetId, tableId) = cfg.dataTable.get
    val table = bq.getTable(datasetId, tableId)
    assert(table != null, "Couldn't get table")
    logger.info(s"Resulting table contains: ${table.getNumRows()} row(s)")
    val destUri = destinationURI(cfg)
    logger.info(s"Writing data to: $destUri")

    val job = table.extract("CSV", destUri)

    cfg.copy(
      destinationUri = Some(destUri),
      state = JobState.RUNNING,
      bqJobId = Some(job.getJobId().getJob()),
      documentCount = Some(table.getNumRows().longValue()))
  }
}
