package assignmentsender

import java.net.URL

import assignmentsender.email.EmailSender
import assignmentsender.preparator.AssignmentPreparator
import assignmentsender.s3.S3Helper
import com.amazonaws.services.s3.AmazonS3

import scala.annotation.tailrec
import scala.util.Try

class AssignmentSender(val s3Client: AmazonS3,
                       assignmentPicker: AssignmentChooserF,
                       assignmentPreparator: AssignmentPreparatorF,
                       emailSender: EmailSender)
                      (implicit submissionS3Bucket: String = sys.env("LCRE_CANDIDATE_SUBMISSIONS_S3_BUCKET"),
                       assignmentsS3Bucket: String = sys.env("LCRE_ASSIGNMENTS_BUCKET"))
  extends Logging
    with S3Helper {


  def send(candidates: List[Candidate]): List[Try[Result]] = {

    @tailrec
    def sendR(candidates: List[Candidate], results: List[Try[Result]] = List()): List[Try[Result]] = {
      candidates match {
        case candidate :: remaining =>
          sendR(remaining, send(candidate, createPreSignedUrl(submissionS3Bucket, s"assignment-${candidate.id}.zip")) :: results)
        case Nil =>
          results
      }
    }

    sendR(candidates)
  }

  private[assignmentsender] def send(candidate: Candidate,
                                     submissionUrl: Try[URL]): Try[Result] = {

    submissionUrl
      .flatMap(sUrl => assignmentPicker.andThen(assignmentPreparator.curried)(candidate)(candidate.id)(sUrl))
      .flatMap(assignmentLocalPath => putFileInS3Bucket(assignmentsS3Bucket, assignmentLocalPath.toFile))
      .flatMap(candidateAssignmentUrl => {
        logger.info(s"Assignment uploaded to S3 $candidateAssignmentUrl. Sending email to ${candidate.email}")
        emailSender.sendEmail(candidate, candidateAssignmentUrl)
      })
      .map(sendEmailResult => {
        logger.info(s"Received email response $sendEmailResult")
        Result(sendEmailResult.getMessageId, sendEmailResult.getSdkHttpMetadata.getHttpStatusCode)
      })
  }


}

object AssignmentSender {
  def apply(s3Client: AmazonS3, assignmentDecider: AssignmentChooserF, emailSender: EmailSender): AssignmentSender =
    new AssignmentSender(s3Client, assignmentDecider, new AssignmentPreparator(), emailSender)
}



