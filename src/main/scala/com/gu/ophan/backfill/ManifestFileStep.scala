package com.gu.ophan.backfill

object ManifestFileStep extends JsonHandler[Seq[JobConfig], String] {

  override def process(input: Seq[JobConfig])(implicit env: Env): String = {
    logger.info("creating manifest file")
    "complete"
  }

}
