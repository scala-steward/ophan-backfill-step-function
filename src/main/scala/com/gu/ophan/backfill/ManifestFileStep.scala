package com.gu.ophan.backfill

import java.time.LocalDate

import upickle.default._
import java.nio.channels.Channels

object ManifestFileStep extends JsonHandler[Seq[JobConfig], String] {

  override def process(input: Seq[JobConfig])(implicit env: Env): String = {
    (for (cfg <- input.headOption) yield {
      logger.info("creating manifest file")

      val prefix = ExtractDataStep.pathPrefix(cfg)

      val manifestLines = input.map { jobConfig =>
        ManifestFileLine(jobConfig.startDateInc, jobConfig.documentCount)
      }

      uploadObject(prefix = prefix, manifest = manifestLines)
    }).getOrElse("<<EMPTY>>")
  }

  import com.google.cloud.storage.BlobId
  import com.google.cloud.storage.BlobInfo
  import com.google.cloud.storage.StorageOptions

  def uploadObject(
    prefix: String,
    manifest: Seq[ManifestFileLine],
    projectId: String = "datatech-platform-prod",
    bucketName: String = "gu-ophan-backfill-prod",
    objectName: String = "manifest.json"): String = {

    val storage = StorageOptions.newBuilder.setProjectId(projectId).build.getService
    val blobId = BlobId.of(bucketName, prefix + "/" + objectName)
    val blobInfo = BlobInfo.newBuilder(blobId).build

    logger.info(s"writing manifest file: ${blobInfo.getName()}")

    val out = Channels.newWriter(storage.writer(blobInfo), "UTF-8")

    try {
      upickle.default.writeTo(manifest, out)
    } finally {
      out.close()
    }
    blobInfo.getName()
  }
}

case class ManifestFileLine(date: LocalDate, count: Option[Long])

object ManifestFileLine {
  import JsonHelpers._
  implicit val writer: upickle.default.Writer[ManifestFileLine] =
    upickle.default.macroW[ManifestFileLine]
}
