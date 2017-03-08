package assignmentsender

import com.amazonaws.services.dynamodbv2.model.{AttributeValue, StreamRecord}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord
import org.mockito.Mockito.{verify, when}

import scala.collection.convert.wrapAsJava._

class AssignmentSenderHandlerSpec
  extends BaseTestSpec {


  describe("AssignmentSenderHandler") {
    it("should return OK response when INSERT event is received") {
      val handler = new AssignmentSenderHandler()
      val event = mock[DynamodbEvent]
      val record = mock[DynamodbStreamRecord]
      when(record.getEventName).thenReturn("INSERT")
      val experience = new AttributeValue()
      experience.setN("5")
      val attributeMap = Map(
        "id" -> new AttributeValue("123"),
        "experience" -> experience,
        "fullname" -> new AttributeValue("Foo Bar"),
        "email" -> new AttributeValue("foo@bar.com"),
        "skills" -> new AttributeValue("java")
      )

      val streamRecord = mock[StreamRecord]
      when(record.getDynamodb).thenReturn(streamRecord)
      when(streamRecord.getNewImage).thenReturn(attributeMap)
      when(event.getRecords).thenReturn(List(record))

      val assignmentSender = mock[AssignmentSender]

      val response: String = handler.handleRequest(event, assignmentSender)

      response should be("OK")
      verify(record).getEventName
      verify(record).getDynamodb
      verify(streamRecord).getNewImage
      verify(assignmentSender).send(List(Candidate("123", "Foo Bar", "foo@bar.com", 5, "java")))
    }

    it("should return 'NOTHING_TO_PROCESS' when no INSERT events are received") {
      val handler = new AssignmentSenderHandler()
      val event = mock[DynamodbEvent]
      val record = mock[DynamodbStreamRecord]
      when(record.getEventName).thenReturn("MODIFY")
      when(event.getRecords).thenReturn(List(record))
      val assignmentSender = mock[AssignmentSender]
      val response: String = handler.handleRequest(event, assignmentSender)
      response should be("NOTHING_TO_PROCESS")
      verify(record).getEventName
    }
  }

}
