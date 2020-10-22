package com.gu.ophan.backfill

/**
 * Step 1: Initiate the data extraction job
 */

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import java.time.temporal.ChronoUnit
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

case class JobConfig(startDateInc: Instant, endDateExc: Instant)

case class Env(app: String, stack: String, stage: String) {
  override def toString: String = s"App: $app, Stack: $stack, Stage: $stage"
}

object Env {
  def apply(): Env = Env(
    Option(System.getenv("App")).getOrElse("DEV"),
    Option(System.getenv("Stack")).getOrElse("DEV"),
    Option(System.getenv("Stage")).getOrElse("DEV"))
}

object InitBackfill {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // sometimes you just have to admire scala ... *sometimes* ...
  implicit val dateReader = upickle.default.reader[String].map(Instant.parse _)
  implicit val cfgReader = upickle.default.macroR[JobConfig]

  def parseInput(input: InputStream): JobConfig = upickle.default.read[JobConfig](input)

  def handler(cfgInput: InputStream, context: Context): String = {
    val env = Env()
    val cfg = parseInput(cfgInput)
    val res = process(cfg, env)
    logger.info(s"jobid: ${res}")
    res
  }

  def formatDate(dt: Instant) = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dt.atZone(ZoneId.of("Europe/London")))

  def querySrc(cfg: JobConfig) = {
    // this is just an example for testing, it doesn't belong here!
    s"""
SELECT count(*) FROM public.pageview
  WHERE received_date >= date"${formatDate(cfg.startDateInc)}" AND
        received_date < date"${formatDate(cfg.endDateExc.plus(7, ChronoUnit.DAYS))}" AND
        event_timestamp >= timestamp"${cfg.startDateInc}" AND event_timestamp < timestamp"${cfg.endDateExc}";
"""
  }

  /*
   * I recommend to put your logic outside of the handler
   */
  def process(cfg: JobConfig, env: Env): String = {
    val creds = Auth.getCredentials(env)
    val bq = new BigQuery(env)
    val src = querySrc(cfg)
    logger.info(s"Sending query: $src")
    bq.query(src, dryRun = false)
  }
}
