package com.gu.ophan.backfill

/**
 * Step 1: Initiate the data extraction job
 */

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import java.time.temporal.ChronoUnit
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.io.OutputStream

object InitBackfill extends SimpleHandler[JobConfig] {

  def formatDate(dt: Instant) =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")
      .format(dt.atZone(ZoneId.of("Europe/London")))

  def querySrc(cfg: JobConfig) = {
    // this is just an example for testing, it doesn't belong here!
    s"""
SELECT count(*) FROM public.pageview
  WHERE received_date >= date"${formatDate(cfg.startDateInc)}" AND
        received_date < date"${formatDate(cfg.endDateExc.plus(7, ChronoUnit.DAYS))}" AND
        event_timestamp >= timestamp"${cfg.startDateInc}" AND event_timestamp < timestamp"${cfg.endDateExc}";
"""
  }

  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    val creds = Auth.getCredentials(env)
    val bq = new BigQuery(env)
    val src = querySrc(cfg)
    logger.info(s"Sending query: $src")
    val jobId = bq.query(src, dryRun = false)
    cfg.copy(jobId = Some(jobId))
  }
}
