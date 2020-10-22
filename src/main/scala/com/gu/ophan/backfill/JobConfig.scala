package com.gu.ophan.backfill

import java.time.Instant

case class JobConfig(
  startDateInc: Instant,
  endDateExc: Instant,
  jobStarted: Instant = Instant.now(),
  bqJobId: Option[String] = None,
  complete: Boolean = false)

object JobConfig {
  // sometimes you just have to admire scala ... *sometimes* ...
  implicit val dateReader = upickle.default.readwriter[String].bimap(
    (_: Instant).toString, Instant.parse _)

  implicit val readWriter = upickle.default.macroRW[JobConfig]

}
