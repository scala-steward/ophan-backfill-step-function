package com.gu.ophan.backfill

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId

/**
 * Step 3: Extract data from temporary table created during job
 */

object ExtractDataStep extends SimpleHandler[JobConfig] {

  def formatDate(dt: Instant) =
    DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")
      .format(dt.atZone(ZoneId.of("Europe/London")))

  def datestamp(cfg: JobConfig) =
    s"${formatDate(cfg.startDateInc)}--${formatDate(cfg.endDateExc)}"

  def destinationURI(cfg: JobConfig)(implicit env: Env) =
    s"gs://gu-ophan-backfill-${env.stage.toLowerCase}/backfill.${datestamp(cfg)}.csv"

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    val bq = new BigQuery
    val (datasetId, tableId) = cfg.dataTable.get
    val table = bq.getTable(datasetId, tableId)
    assert(table != null, "Couldn't get table")
    logger.info(s"Resulting table contains: ${table.getNumRows()} row(s)")
    val destUri = destinationURI(cfg)
    logger.info(s"Writing data to: $destUri")

    val job = table.extract("CSV", destUri)

    cfg.copy(destinationUri = Some(destUri), state = JobState.RUNNING)
  }
}
