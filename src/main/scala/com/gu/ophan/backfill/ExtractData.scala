package com.gu.ophan.backfill

/**
 * Step 3: Extract data from temporary table created during job
 */

object ExtractDataStep extends SimpleHandler[JobConfig] {
  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    val bq = new BigQuery
    val (datasetId, tableId) = cfg.dataTable.get
    val table = bq.getTable(datasetId, tableId)
    assert(table != null, "Couldn't get table")
    logger.info(s"Resulting table contains: ${table.getNumRows()} row(s)")
    cfg
  }
}
