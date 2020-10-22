package com.gu.ophan.backfill

import java.io.ByteArrayInputStream

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
    QueryJobState.process(
      InitBackfill.process(cfg))
  }
}
