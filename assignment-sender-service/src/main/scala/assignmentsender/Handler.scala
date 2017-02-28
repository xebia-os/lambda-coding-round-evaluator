package assignmentsender

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.util.{Date, UUID}

import com.amazonaws.HttpMethod
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{CannedAccessControlList, GeneratePresignedUrlRequest, PutObjectRequest}
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._
import org.zeroturnaround.zip.{FileSource, ZipUtil}

import scala.collection.convert.WrapAsScala._
import scala.util.{Failure, Success, Try}

class Handler extends RequestHandler[DynamodbEvent, String] {


  private val s3Client = AmazonS3ClientBuilder.defaultClient()

  private val emailClient = new AmazonSimpleEmailServiceClient()

  private def toCandidate(r: DynamodbStreamRecord): Candidate = {
    val image = r.getDynamodb.getNewImage
    val experience = image.get("experience").getN.toInt
    val candidateId = image.get("id").getS
    val skills = image.get("skills").getS
    val fullname = image.get("fullname").getS
    val email = image.get("email").getS
    Candidate(candidateId, fullname, email, experience, skills)
  }

  def handleRequest(event: DynamodbEvent, context: Context): String = {
    println("Received DynamoDB Event")
    event.getRecords.foreach(println)
    val insertRecords = event.getRecords.filter(r => r.getEventName() == "INSERT")
    if (insertRecords.isEmpty) {
      println("Nothing to process as no INSERT event received.")
      "OK"
    } else {
      val candidates = insertRecords
        .map(r => toCandidate(r))
      println(s"Working on candidates ${candidates}")
      processCandidates(candidates.toList)
      "OK"
    }

  }

  def processCandidates(candidates: List[Candidate]) = {
    candidates
      .map(c => (c, assignmentS3Url(c)))
      .map { case (c, s3Url) => {
        val submissionS3Bucket = sys.env("LCRE_CANDIDATE_SUBMISSIONS_S3_BUCKET")
        val assignmentsS3Bucket = sys.env("LCRE_ASSIGNMENTS_BUCKET")
        val gradlePropertiesPath: Path = writeGradleProperties(c, submissionS3Bucket)
        val downloadedAssignmentZip: File = downloadAssignmentAndAddGradleWrapperProperties(c, s3Url, gradlePropertiesPath)
        s3Client.putObject(new PutObjectRequest(assignmentsS3Bucket, downloadedAssignmentZip.getName, downloadedAssignmentZip).withCannedAcl(CannedAccessControlList.PublicRead))
        val candidateAssignmentUrl = s3Client.getUrl(assignmentsS3Bucket, downloadedAssignmentZip.getName)
        println(s"Assignment uploaded to $candidateAssignmentUrl")
        sendEmail(c, candidateAssignmentUrl) match {
          case Success(_) => "OK"
          case Failure(_) => "Failure"
        }
      }
      }
  }

  private def assignmentS3Url(candidate: Candidate): String = {
    val experience = candidate.experience
    if (experience > 0 && experience <= 5) {
      "https://s3.amazonaws.com/" + sys.env("LCRE_ASSIGNMENTS_BUCKET") + "/assignment1.zip"
    } else if (experience > 5 && experience <= 8) {
      "https://s3.amazonaws.com/" + sys.env("LCRE_ASSIGNMENTS_BUCKET") + "/assignment2.zip"
    } else {
      "https://s3.amazonaws.com/" + sys.env("LCRE_ASSIGNMENTS_BUCKET") + "/assignment3.zip"
    }
  }

  private def downloadAssignmentAndAddGradleWrapperProperties(c: Candidate, s3Url: String, gradlePropertiesPath: Path) = {
    import sys.process._
    val assignmentZipName = s3Url.substring(s3Url.lastIndexOf("/")).replace(".zip", "") + "-" + c.id + ".zip"
    val downloadedAssignmentZip = Paths.get("/tmp", assignmentZipName).toFile
    (new URL(s3Url) #> downloadedAssignmentZip).!!

    val gradlePropertiesFile = Paths.get(s3Url.substring(s3Url.lastIndexOf("/")).replace(".zip", ""), "myapp", "gradle.properties").toString

    ZipUtil.addEntry(downloadedAssignmentZip, new FileSource(gradlePropertiesFile, gradlePropertiesPath.toFile))
    downloadedAssignmentZip
  }

  private def writeGradleProperties(c: Candidate, submissionS3Bucket: String) = {
    val assignmentSubmissionUrl = createPreSignedUrl(submissionS3Bucket, s"assignment-${c.id}.zip")
    val tmpDir = Paths.get("/tmp", UUID.randomUUID().toString)
    tmpDir.toFile.mkdir()
    val gradlePropertiesPath = tmpDir.resolve("gradle.properties")
    Files.write(
      gradlePropertiesPath,
      List(s"assignmentName=assignment-${c.id}.zip", s"assignmentSubmissionUrl=$assignmentSubmissionUrl")
        .mkString("\n").getBytes())
    gradlePropertiesPath
  }

  private def sendEmail(c: Candidate, candidateAssignmentUrl: URL): Try[SendEmailResult] = {
    Try {
      val destination = new Destination().withToAddresses(c.email)
      val subject = new Content().withData("Coding Round with Awesome Company")
      val textBody = new Content().withData(
        s"""
           |Hello ${c.fullname},

           |Thanks for applying position with us. Please download assignment from $candidateAssignmentUrl.
           |Please read the instructions before submitting assignment.

           |Thanks,
           |Recruitment Team
           |""".
          stripMargin)

      val body = new Body().withText(textBody)
      val message = new Message().withSubject(subject).withBody(body)

      val request = new SendEmailRequest()
        .withSource(sys.env("SOURCE_EMAIL"))
        .withDestination(destination)
        .withMessage(message)

      emailClient.sendEmail(request)
    }

  }

  private def createPreSignedUrl(bucket: String, filename: String): URL = {
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
