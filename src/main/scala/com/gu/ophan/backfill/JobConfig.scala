package com.gu.ophan.backfill

import java.time.LocalDate.ofEpochDay
import java.time.ZoneOffset.UTC
import java.time.{ Instant, LocalDate }

object JobState extends Enumeration {
  val INIT, RUNNING, WAITING, ERROR, COMPLETED = Value
}

case class JobConfig(
  startDateInc: LocalDate,
  endDateExc: LocalDate,
  executionId: String, // the step function should be configured to provide this
  jobStartTime: Instant = Instant.now(),
  bqJobId: Option[String] = None,
  state: JobState.Value = JobState.INIT,
  dataTable: Option[(String, String)] = None,
  destinationUri: Option[String] = None,
  async: Boolean = true,
  errorMsg: Option[String] = None,
  documentCount: Option[Long] = None) {

  val queryTimeDeclarations: String = s"""
    |DECLARE startTimeInclusive TIMESTAMP DEFAULT TIMESTAMP("${startDateInc.atStartOfDay(UTC).toInstant}");
    |DECLARE endTimeExclusive TIMESTAMP DEFAULT TIMESTAMP("${endDateExc.atStartOfDay(UTC).toInstant}");
  """.stripMargin

  def asDayJobs: Seq[JobConfig] = {
    for (day <- startDateInc.toEpochDay until endDateExc.toEpochDay) yield copy(startDateInc = ofEpochDay(day), endDateExc = ofEpochDay(day + 1))
  }
}

object JobConfig {
  import JsonHelpers._

  implicit val jobState = upickle.default.readwriter[String]
    .bimap[JobState.Value](_.toString, JobState.withName)

  implicit val readWriter = upickle.default.macroRW[JobConfig]

}
