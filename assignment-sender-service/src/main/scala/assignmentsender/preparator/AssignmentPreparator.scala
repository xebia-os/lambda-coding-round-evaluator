package assignmentsender.preparator

import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.UUID

import assignmentsender._
import assignmentsender.preparator.AssignmentPreparator.{addGradlePropertiesToAssignmentZip, assignmentDownloader, createGradleProperties}
import org.zeroturnaround.zip.{FileSource, ZipUtil}

import scala.util.Try

class AssignmentPreparator extends AssignmentPreparatorF {

  override def apply(assignment: Assignment, candidateId: CandidateId, assignmentSubmissionUrl: URL): Try[AssignmentLocalPath] = {
    Try {
      val gradlePropertiesPath = createGradleProperties(candidateId, assignmentSubmissionUrl)
      val assignmentLocalFile = assignmentDownloader(assignment.assignmentUrl, candidateId)
      addGradlePropertiesToAssignmentZip(assignmentLocalFile, gradlePropertiesPath)
      assignmentLocalFile.toPath
    }
  }

}

object AssignmentPreparator {
  private[assignmentsender] def assignmentDownloader: AssignmentDownloaderF = (assignmentUrl, candidateId) => {
    import sys.process._
    val assignmentZipName = assignmentUrl.substring(assignmentUrl.lastIndexOf("/")).replace(".zip", "") + "-" + candidateId + ".zip"
    val assignment = Paths.get("/tmp", assignmentZipName).toFile
    (new URL(assignmentUrl) #> assignment).!!
    assignment
  }

  private[assignmentsender] def addGradlePropertiesToAssignmentZip: AddGradlePropertiesToZipF = (assignmentLocalFile, gradlePropertiesPath) => {
    val gradlePropertiesFile = Paths.get("gradle.properties").toString
    ZipUtil.addEntry(assignmentLocalFile, new FileSource(gradlePropertiesFile, gradlePropertiesPath.toFile))
    assignmentLocalFile
  }

  private[assignmentsender] def createGradleProperties(candidateId: CandidateId, assignmentSubmissionUrl: URL) = {
    val tmpDir = Paths.get("/tmp", UUID.randomUUID().toString)
    tmpDir.toFile.mkdir()
    val gradlePropertiesPath = tmpDir.resolve("gradle.properties")
    Files.write(
      gradlePropertiesPath,
      List(s"assignmentName=assignment-$candidateId.zip", s"assignmentSubmissionUrl=$assignmentSubmissionUrl").mkString("\n").getBytes()
    )
    gradlePropertiesPath
  }
}
