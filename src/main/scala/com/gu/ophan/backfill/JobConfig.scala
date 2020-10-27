package com.gu.ophan.backfill

import java.time.Instant

object JobState extends Enumeration {
  val INIT, RUNNING, WAITING, ERROR, COMPLETED = Value
}

case class JobConfig(
  startDateInc: Instant,
  endDateExc: Instant,
  jobStartTime: Instant = Instant.now(),
  bqJobId: Option[String] = None,
  state: JobState.Value = JobState.INIT,
  dataTable: Option[(String, String)] = None,
  destinationUri: Option[String] = None,
  async: Boolean = true,
  errorMsg: Option[String] = None) {

  val queryTimeDeclarations: String = s"""
    |DECLARE startTimeInclusive TIMESTAMP DEFAULT TIMESTAMP("$startDateInc");
    |DECLARE endTimeExclusive TIMESTAMP DEFAULT TIMESTAMP("$endDateExc");
  """.stripMargin
}

object JobConfig {
  // sometimes you just have to admire scala ... *sometimes* ...
  implicit val dateReader = upickle.default.readwriter[String].bimap(
    (_: Instant).toString, Instant.parse _)

  implicit val jobState = upickle.default.readwriter[String].bimap(
    (_: JobState.Value).toString, JobState.withName _)

  implicit val readWriter = upickle.default.macroRW[JobConfig]

}
