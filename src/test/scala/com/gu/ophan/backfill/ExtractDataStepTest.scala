package com.gu.ophan.backfill

import org.scalatest.flatspec._
import org.scalatest.matchers._

import java.time.LocalDate

class ExtractDataStepTest extends AnyFlatSpec with should.Matchers {

  val MaxBytesAvailableForSingleFilenameLine: Int = {
    val BytesAvailableForAllFilenameLinesInState = {
      val StepFunctionMaxStateSizeInBytes = 262144 // https://docs.aws.amazon.com/step-functions/latest/dg/limits.html#service-limits-task-executions
      val ExtraOneOffBoilerplate = 100 // all the stuff that is *not* the filename lines in the state

      StepFunctionMaxStateSizeInBytes - ExtraOneOffBoilerplate
    }
    val NumFilesInLargestBackfill = 2 * 365 * 9

    BytesAvailableForAllFilenameLinesInState / NumFilesInLargestBackfill
  }

  val MaxSizeOfWildcardFormattedFileName: Int = {
    val WildcardFileNumberLength = 12 // eg '000000000001' https://cloud.google.com/bigquery/docs/exporting-data#exporting_data_into_one_or_more_files
    val ExtraBoilerplateCharactersPerFileName = """    "",""".length // eg __    "backfill.xxxx.csv",__ after JSON formatting

    MaxBytesAvailableForSingleFilenameLine - (ExtraBoilerplateCharactersPerFileName + (WildcardFileNumberLength - 1))
  }

  "Dumped filenames" should "be small enough for 2-years-worth of filenames to fit in AWS Step Functions state" in {
    MaxSizeOfWildcardFormattedFileName shouldBe 21 // yes, that is what the big sum ends up at
    val now = LocalDate.now()
    ExtractDataStep.fileNameFormat(now.minusDays(1), now).length should be <= MaxSizeOfWildcardFormattedFileName
  }
}
