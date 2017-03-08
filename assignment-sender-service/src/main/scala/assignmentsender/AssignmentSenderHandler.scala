package assignmentsender

import assignmentsender.chooser.AssignmentChooser
import assignmentsender.email.EmailSender
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder

import scala.collection.convert.WrapAsScala._

class AssignmentSenderHandler
  extends RequestHandler[DynamodbEvent, String]
    with Logging
    with DynamoDBStreamRecordToCandidate {


  def handleRequest(event: DynamodbEvent, context: Context): String = {
    val s3Client = AmazonS3ClientBuilder.defaultClient()
    val emailClient = AmazonSimpleEmailServiceClientBuilder.defaultClient()
    val assignmentSender = AssignmentSender(s3Client, AssignmentChooser(), new EmailSender(emailClient))
    handleRequest(event, assignmentSender)
  }

  private[assignmentsender] def handleRequest(event: DynamodbEvent, assignmentSender: AssignmentSender) = {
    logger.info("Received event from DynamoDB service")
    val insertRecords = event.getRecords.filter(r => r.getEventName == "INSERT")
    if (insertRecords.isEmpty) {
      logger.info("No assignment needs to be sent as there are no INSERT events")
      "NOTHING_TO_PROCESS"
    } else {
      logger.info(s"Received ${insertRecords.size} INSERT event(s).")
      val candidates = insertRecords.map(r => toCandidate(r))
      logger.info(s"Will be sending assignment to following candidate(s) ${candidates.map(_.email).mkString(",")}")
      assignmentSender.send(candidates.toList)
      "OK"
    }
  }
}

trait DynamoDBStreamRecordToCandidate {

  def toCandidate(r: DynamodbStreamRecord): Candidate = {
    val image = r.getDynamodb.getNewImage
    val experience = image.get("experience").getN.toInt
    val candidateId = image.get("id").getS
    val skills = image.get("skills").getS
    val fullname = image.get("fullname").getS
    val email = image.get("email").getS
    Candidate(candidateId, fullname, email, experience, skills)
  }
}
