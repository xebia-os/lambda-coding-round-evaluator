package assignmentsender.s3

import java.io.File
import java.net.URL
import java.util.Date

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{CannedAccessControlList, GeneratePresignedUrlRequest, PutObjectRequest}

import scala.util.Try

trait S3Helper {
  val s3Client: AmazonS3

  def putFileInS3Bucket(bucket: String, fileToUpload: File): Try[URL] = {
    Try {
      s3Client.putObject(new PutObjectRequest(bucket, fileToUpload.getName, fileToUpload).withCannedAcl(CannedAccessControlList.PublicRead))
      s3Client.getUrl(bucket, fileToUpload.getName)
    }
  }

  def createPreSignedUrl(bucket: String, filename: String): Try[URL] = {
    Try {
      val expiration = new Date()
      var msec = expiration.getTime
      msec += 7 * 24 * 1000 * 60 * 60
      expiration.setTime(msec)
      val generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, filename)
      generatePresignedUrlRequest.setMethod(HttpMethod.PUT)
      generatePresignedUrlRequest.setExpiration(expiration)
      s3Client.generatePresignedUrl(generatePresignedUrlRequest)
    }
  }
}
