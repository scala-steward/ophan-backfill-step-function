package com.gu.ophan.backfill

import java.time.Instant
import java.io.ByteArrayInputStream
import java.time.temporal.ChronoUnit

object TestIt {
  val example = """
{
  "startDateInc": "2020-05-10T00:00:00Z",
  "endDateExc":   "2020-05-10T00:05:00Z"
}
"""

  def main(args: Array[String]): Unit = {
    val cfg = upickle.default.read[JobConfig](example)
    implicit val env = Env()

    val steps: Seq[SimpleHandler[JobConfig]] =
      InitBackfillStep :: QueryJobStateStep :: ExtractDataStep :: Nil

    steps.foldLeft(cfg) { (cfg, step) =>
      val res = step.process(cfg)
      println(res)
      res
    }
  }
}
