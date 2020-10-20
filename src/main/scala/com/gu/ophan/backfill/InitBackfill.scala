package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import java.time.Instant
import java.time.temporal.ChronoUnit

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

  /*
   * This is your lambda entry point
   */
  def handler(cfg: JobConfig, context: Context): Unit = {
    val env = Env()
    logger.info(s"Starting $env")
    logger.info(process(cfg, env))
  }

  /*
   * I recommend to put your logic outside of the handler
   */
  def process(cfg: JobConfig, env: Env): String = {
    println("creds: " + Auth.getCredentials(env))
    s"Job for ${cfg.startDateInc} => ${cfg.endDateExc}"
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    println(InitBackfill.process(JobConfig(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS)), Env()))
  }
}
