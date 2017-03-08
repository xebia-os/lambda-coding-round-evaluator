package assignmentsender

import java.net.URL
import java.nio.file.Paths
import java.util

import assignmentsender.email.EmailSender
import com.amazonaws.http.SdkHttpMetadata
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{PutObjectRequest, PutObjectResult}
import com.amazonaws.services.simpleemail.model.SendEmailResult
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.Mockito.when

import scala.util.Success
import scala.collection.convert.wrapAsJava._

class AssignmentSenderSpec extends BaseTestSpec {


  describe("AssignmentSender") {
    it("should send assignment to candidate") {
      val candidate = Candidate("123", "Foo Bar", "foo@bar", 5, "java")
      val assignmentPicker: AssignmentChooserF = (_) => Assignment("http://test.com/assignment.zip", List("java"), 5)
      val preparator: AssignmentPreparatorF = (_, cId, _) => Success(Paths.get("/tmp", s"assignment-$cId.zip"))

      val mockS3Client = mock[AmazonS3]
      val mockEmailSender = mock[EmailSender]
      val sender = new AssignmentSender(mockS3Client, assignmentPicker, preparator, mockEmailSender)("test_submission_bucket", "test_assignment_bucket")

      val assignmentUrl = new URL("http://test.com/assignment.zip")
      when(mockS3Client.putObject(any[PutObjectRequest]())).thenReturn(mock[PutObjectResult])
      when(mockS3Client.getUrl("test_assignment_bucket", "assignment-123.zip")).thenReturn(assignmentUrl)

      val sendEmailResult = new SendEmailResult().withMessageId("messageId")
      val httpMetadata = mock[SdkHttpMetadata]
      when(httpMetadata.getHttpHeaders).thenReturn(Map[String, String]())
      when(httpMetadata.getHttpStatusCode).thenReturn(200)
      sendEmailResult.setSdkHttpMetadata(httpMetadata)
      when(mockEmailSender.sendEmail(candidate, assignmentUrl)).thenReturn(Success(sendEmailResult))

      val result = sender.send(candidate, Success(new URL("http://test.com/assignment.zip")))

      result should be(Success(Result("messageId", 200)))

    }
  }

}
