package com.gu.ophan.backfill

import java.io.{ BufferedWriter, FileWriter }
import java.time.LocalDate

import upickle.default._
import java.nio.ByteBuffer
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object ManifestFileStep extends JsonHandler[Seq[JobConfig], String] {

  override def process(input: Seq[JobConfig])(implicit env: Env): String = {
    (for (cfg <- input.headOption) yield {
      logger.info("creating manifest file")

      val prefix = ExtractDataStep.pathPrefix(cfg)

      val manifestLines = input.map { jobConfig =>
        ManifestFileLine(jobConfig.startDateInc, jobConfig.documentCount)
      }

      uploadObject(prefix = prefix, manifest = manifestLines) match {
        case Success(manifestFile) => manifestFile
        case Failure(err) => err.toString
      }
    }).getOrElse("empty")
  }

  import com.google.cloud.storage.BlobId
  import com.google.cloud.storage.BlobInfo
  import com.google.cloud.storage.StorageOptions
  import java.io.IOException
  import java.nio.file.Files
  import java.nio.file.Paths

  def uploadObject(
    prefix: String,
    manifest: Seq[ManifestFileLine],
    projectId: String = "datatech-platform-prod",
    bucketName: String = "gu-ophan-backfill-prod",
    objectName: String = "manifest.json"): Try[String] = Try {

    val storage = StorageOptions.newBuilder.setProjectId(projectId).build.getService
    val blobId = BlobId.of(bucketName, objectName)
    val blobInfo = BlobInfo.newBuilder(blobId).build

    val w = storage.writer(blobInfo)
    val manifestAsJson = ByteBuffer.wrap(upickle.default.write(manifest).getBytes)
    w.write(manifestAsJson)
    w.close()

    blobInfo.toString
  }
}

case class ManifestFileLine(date: LocalDate, count: Option[Long])

object ManifestFileLine {
  import JsonHelpers._
  implicit val writer: upickle.default.Writer[ManifestFileLine] =
    upickle.default.macroW[ManifestFileLine]
}
