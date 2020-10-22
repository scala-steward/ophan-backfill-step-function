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

    val step1 = InitBackfill.process(cfg)
      .copy(jobStartTime = Instant.now().minus(10, ChronoUnit.MINUTES)) // pretend its been running for 10 minutes
    val step2 = QueryJobState.process(step1)

    println(step2)
  }
}
