package com.gu.ophan.backfill

/**
 * Step 1: Initiate the data extraction job
 */

import scala.io.Source

object InitBackfillStep extends SimpleHandler[JobConfig] {

  val queryWithoutTimeDeclarations: String =
    Source.fromInputStream(getClass.getResourceAsStream("/backfill-query.sql")).getLines().drop(2).mkString("\n")

  def querySrc(cfg: JobConfig): String = cfg.queryTimeDeclarations + "\n" + queryWithoutTimeDeclarations

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    val bq = new BigQuery
    val src = querySrc(cfg)
    logger.info(s"Sending query: $src")
    val jobId = bq.query(src, dryRun = false).getJobId().getJob()
    cfg.copy(bqJobId = Some(jobId))
  }
}
