package com.gu.ophan.backfill

/**
 * Step 4: Check if the data extraction job has completed (this check
 * should be repeated periodically until it has completed)
 */

object AwaitExtractJobStep extends SimpleHandler[JobConfig] with AwaitJob
