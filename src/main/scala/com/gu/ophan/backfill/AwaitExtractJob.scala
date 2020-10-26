package com.gu.ophan.backfill

import com.google.cloud.bigquery.Job

/**
 * Step 4: Check if the data extraction job has completed (this check
 * should be repeated periodically until it has completed)
 */

object AwaitExtractJobStep extends SimpleHandler[JobConfig] with AwaitJob
