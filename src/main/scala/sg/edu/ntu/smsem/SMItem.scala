package sg.edu.ntu.smsem

import sg.edu.ntu.ProjectMD

case class SMItem(projectMD: ProjectMD, var sems: List[SMSem]) {

  def appendSem(sem: SMSem): Unit = {
    logger.info(s"adding ${sem.getClass} to ${projectMD}")
    sems.appended(sem)
  }

}

object SMItem {

}
