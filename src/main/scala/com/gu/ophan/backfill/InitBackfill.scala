package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.io.InputStream

case class JobConfig(startDateInc: Instant, endDateExc: Instant)
object JobConfig {
  def fromJson(json: ujson.Value): JobConfig = {
    val obj = json.obj
    JobConfig(
      startDateInc = Instant.parse(obj("startDateInc").str),
      endDateExc = Instant.parse(obj("endDateInc").str))
  }
}

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

  //  def parseInput(input: Map[String, String]): JobConfig = ???

  def handler(cfgInput: InputStream, context: Context): Unit = {
    val env = Env()
    logger.info(s"Starting $env")
    val cfg = JobConfig.fromJson(ujson.read(cfgInput))
    logger.info(s"config: $cfg")
  }

  /*
   * I recommend to put your logic outside of the handler
   */
  def process(cfg: JobConfig, env: Env): String = {
    s"Job for ${cfg.startDateInc} => ${cfg.endDateExc}"
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    println(InitBackfill.process(JobConfig(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS)), Env()))
  }
}
