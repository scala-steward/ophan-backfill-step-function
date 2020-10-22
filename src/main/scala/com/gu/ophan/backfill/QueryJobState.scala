package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }

/**
 * Step 2: Check if the data extraction job has completed (this check should be repeated periodically until it has completed)
 */

object QueryJobState extends SimpleHandler[JobConfig] {
  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    logger.info(s"checking status of jobid: ${cfg.jobId.get}")
    cfg.copy(complete = false)
  }
}
