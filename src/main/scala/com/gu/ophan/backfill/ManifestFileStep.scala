package com.gu.ophan.backfill

import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDate

object ManifestFileStep extends JsonHandler[Seq[JobConfig], String] {

  override def process(input: Seq[JobConfig])(implicit env: Env): String = {
    logger.info("creating manifest file")
    val manifestLines = input.map { jobConfig =>
      ManifestFileLine(jobConfig.startDateInc, jobConfig.documentCount)
    }

    UploadObject.uploadObject(manifest = manifestLines)
    "complete"
  }

  import com.google.cloud.storage.BlobId
  import com.google.cloud.storage.BlobInfo
  import com.google.cloud.storage.StorageOptions
  import java.io.IOException
  import java.nio.file.Files
  import java.nio.file.Paths

  //https://cloud.google.com/storage/docs/uploading-objects#storage-upload-object-code-sample
  object UploadObject {
    @throws[IOException]
    def uploadObject(
      projectId: String = "datatech-platform-prod",
      bucketName: String = "gu-ophan-backfill-prod/debug-1st-feb", //todo
      objectName: String = "manifest.json",
      manifest: Seq[ManifestFileLine]): Unit = { // The ID of your GCP project
      // String projectId = "your-project-id";
      // The ID of your GCS bucket
      // String bucketName = "your-unique-bucket-name";
      // The ID of your GCS object
      // String objectName = "your-object-name";
      // The path to your file to upload
      // String filePath = "path/to/your/file"
      val storage = StorageOptions.newBuilder.setProjectId(projectId).build.getService
      val blobId = BlobId.of(bucketName, objectName)
      val blobInfo = BlobInfo.newBuilder(blobId).build

      val w: BufferedWriter = new BufferedWriter(new FileWriter(objectName))
      val manifestAsJson = upickle.default.write[Seq[ManifestFileLine]](manifest)
      w.write(manifestAsJson)
      w.close()

      storage.create(blobInfo, Files.readAllBytes(Paths.get(objectName)))
      System.out.println("File " + " uploaded to bucket " + bucketName + " as " + objectName)
    }
  }

}

case class ManifestFileLine(date: LocalDate, count: Option[Long])