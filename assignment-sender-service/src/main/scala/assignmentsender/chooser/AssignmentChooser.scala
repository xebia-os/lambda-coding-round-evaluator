package assignmentsender.chooser

import assignmentsender.{Assignment, AssignmentChooserF, Candidate}

class AssignmentChooser(assignmentsS3Bucket: String)
  extends AssignmentChooserF {

  override def apply(candidate: Candidate): Assignment = {
    val experience = candidate.experience
    val skills = List(candidate.skills)
    if (experience > 0 && experience <= 5) {
      Assignment(
        s"https://s3.amazonaws.com/$assignmentsS3Bucket/assignment1.zip",
        skills,
        experience
      )
    } else if (experience > 5 && experience <= 8) {
      Assignment(
        s"https://s3.amazonaws.com/$assignmentsS3Bucket/assignment2.zip",
        skills,
        experience
      )
    } else {
      Assignment(
        s"https://s3.amazonaws.com/$assignmentsS3Bucket/assignment3.zip",
        skills,
        experience
      )
    }
  }
}

object AssignmentChooser {
  def apply(assignmentsS3Bucket: String = sys.env("LCRE_ASSIGNMENTS_BUCKET")): AssignmentChooser = new AssignmentChooser(assignmentsS3Bucket)
}


