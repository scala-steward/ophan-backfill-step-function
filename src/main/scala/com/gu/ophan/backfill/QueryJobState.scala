package com.gu.ophan.backfill

import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.{ Logger, LoggerFactory }
import java.time.{ Duration, Instant }

/**
 * Step 2: Check if the data extraction job has completed (this check should be repeated periodically until it has completed)
 */

object QueryJobState extends SimpleHandler[JobConfig] {
  def process(cfg: JobConfig)(implicit env: Env): JobConfig = {
    logger.info(s"checking status of jobid: ${cfg.bqJobId.get}")

    // pretend it is complete after it runs for 3 minutes
    val newState =
      if ((Duration.between(cfg.jobStartTime, Instant.now()) compareTo Duration.ofMinutes(3)) > 0)
        JobState.COMPLETED
      else
        JobState.RUNNING
    cfg.copy(state = newState)
  }
}
