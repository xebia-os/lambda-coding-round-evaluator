import java.io.File
import java.net.URL
import java.nio.file.Path

import scala.util.Try

package object assignmentsender {

  case class Result(messageId: String, httpStatusCode: Int)

  case class Assignment(assignmentUrl: String, skills: List[String], experience: Int)

  case class Candidate(id: String, fullname: String, email: String, experience: Int, skills: String)

  type CandidateId = String
  type AssignmentUrl = String
  type AssignmentLocalFile = File
  type AssignmentLocalPath = Path

  type AssignmentChooserF = Candidate => Assignment
  type AssignmentPreparatorF = ((Assignment, CandidateId, URL) => Try[AssignmentLocalPath])
  type AssignmentDownloaderF = (AssignmentUrl, CandidateId) => AssignmentLocalFile
  type AddGradlePropertiesToZipF = (AssignmentLocalFile, Path) => AssignmentLocalFile

}
