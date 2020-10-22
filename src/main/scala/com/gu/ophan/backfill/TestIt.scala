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
    val cfg = InitBackfill.parseInput(new ByteArrayInputStream(example.getBytes()))

    QueryJobState.process(
      InitBackfill.process(cfg, Env()))
  }
}
