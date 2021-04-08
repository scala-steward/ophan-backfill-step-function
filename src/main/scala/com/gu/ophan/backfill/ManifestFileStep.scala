package com.gu.ophan.backfill

import java.time.LocalDate

import upickle.default._
import java.nio.channels.Channels
import com.google.cloud.storage.{ BlobId, BlobInfo, StorageOptions }

object ManifestFileStep extends JsonHandler[Seq[JobConfig], String] {

  override def process(input: Seq[JobConfig])(implicit env: Env): String = {
    (for (cfg <- input.headOption) yield {
      logger.info("creating manifest file")

      // if we use the same function we will definitely end up in the same place
      val prefix = ExtractDataStep.pathPrefix(cfg)

      val manifestLines = input.map { jobConfig =>
        ManifestFileLine(jobConfig.startDateInc, jobConfig.documentCount.get)
      }

      uploadObject(prefix = prefix, manifest = manifestLines)
    }).getOrElse("<<EMPTY>>")
  }

  def uploadObject(
    prefix: String,
    manifest: Seq[ManifestFileLine],
    projectId: String = "datatech-platform-prod",
    bucketName: String = "gu-ophan-backfill-prod",
    objectName: String = "manifest.json")(implicit env: Env): String = {

    val storage = StorageOptions.newBuilder
      .setProjectId(projectId)
      .setCredentials(Auth.getCredentials(env))
      .build
      .getService
    val blobId = BlobId.of(bucketName, prefix + "/" + objectName)
    val blobInfo = BlobInfo.newBuilder(blobId).build

    logger.info(s"writing manifest file: ${blobInfo} (prefix=[$prefix])")

    val out = Channels.newWriter(storage.writer(blobInfo), "UTF-8")

    try {
      upickle.default.writeTo(manifest, out)
    } finally {
      out.close()
    }
    s"gs://${blobId.getBucket}/${blobId.getName}"
  }
}

case class ManifestFileLine(date: LocalDate, count: Long)

object ManifestFileLine {
  import JsonHelpers._
  implicit val writer: upickle.default.Writer[ManifestFileLine] =
    upickle.default.macroW[ManifestFileLine]
}
