package assignmentsender.email

import java.net.URL

import assignmentsender.Candidate
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model._

import scala.util.Try

class EmailSender(emailClient: AmazonSimpleEmailService, sourceEmail: String = sys.env("SOURCE_EMAIL")) {

  def sendEmail(candidate: Candidate, candidateAssignmentUrl: URL): Try[SendEmailResult] = {
    Try {
      val destination = new Destination().withToAddresses(candidate.email)
      val subject = new Content().withData("Coding Round with Awesome Company")
      val textBody = new Content().withData(
        s"""
           |Hello ${candidate.fullname},

           |Thanks for applying position with us. Please download assignment from $candidateAssignmentUrl.
           |Please read the instructions mentioned in instructions.pdf before submitting assignment.

           |Thanks,
           |Recruitment Team
           |""".
          stripMargin)

      val body = new Body().withText(textBody)
      val message = new Message().withSubject(subject).withBody(body)

      val request = new SendEmailRequest()
        .withSource(sourceEmail)
        .withDestination(destination)
        .withMessage(message)

      emailClient.sendEmail(request)
    }

  }

}
