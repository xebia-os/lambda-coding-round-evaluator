package assignmentsender

import java.net.URL
import java.nio.file.{Files, Paths}

import assignmentsender.preparator.AssignmentPreparator
import assignmentsender.preparator.AssignmentPreparator.{addGradlePropertiesToAssignmentZip, assignmentDownloader, createGradleProperties}
import org.zeroturnaround.zip.ZipUtil

import scala.collection.convert.wrapAsScala._
import scala.util.Success

class AssignmentPreparatorSpec extends BaseTestSpec {

  private val assignmentUrl = Paths.get("src", "test", "resources", "assignment.zip").toUri.toURL.toString

  describe("AssignmentPreparator") {

    it("should download assignment zip to '/tmp' directory") {
      val assignmentPath = assignmentDownloader(assignmentUrl, "123")
      assignmentPath.exists() should be(true)
      assignmentPath should be(Paths.get("/tmp", "assignment-123.zip").toFile)
    }

    it("should add gradle.properties to assignment zip") {
      assignmentDownloader(assignmentUrl, "123")
      val assignmentFile = Paths.get("/tmp", "assignment-123.zip").toFile
      addGradlePropertiesToAssignmentZip(assignmentFile, Paths.get("src", "test", "resources", "gradle.properties"))
      ZipUtil.containsEntry(assignmentFile, "gradle.properties") should be(true)
    }

    it("should write gradle.properties file to '/tmp' directory") {
      val gradleProperties = createGradleProperties("123", new URL("https://example.com/submit"))
      gradleProperties.toFile.exists() should be(true)
      Files.readAllLines(gradleProperties).mkString("<------->") should be(
        "assignmentName=assignment-123.zip<------->assignmentSubmissionUrl=https://example.com/submit")
    }

    it("should prepare assignment to be sent to candidate") {
      val assignmentPreparator = new AssignmentPreparator()
      val assignmentLocalPath = assignmentPreparator(Assignment(assignmentUrl, List(), 5), "123", new URL("https://example.com/submit"))
      assignmentLocalPath should be(Success(Paths.get("/tmp", "assignment-123.zip")))
    }
  }


}
