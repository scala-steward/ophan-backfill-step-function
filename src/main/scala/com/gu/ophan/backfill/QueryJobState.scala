package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }

/**
 * Step 2: Check if the data extraction job has completed (this check should be repeated periodically until it has completed)
 */

object QueryJobState {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def handler(jobId: String, context: Context): Boolean =
    process(jobId)

  def process(jobId: String): Boolean = {
    logger.info(s"checking status of jobid: ${jobId}")
    false
  }
}
