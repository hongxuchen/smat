package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD

import scala.collection.mutable.ListBuffer

@SerialVersionUID(100L)
case class SMItem(projectMD: ProjectMD, sems: ListBuffer[SMSem]) extends Serializable {

  def appendSem(sem: SMSem): Unit = {
    logger.info(s"adding ${sem.getClass} to ${projectMD}")
    sems.append(sem)
  }

  def analyze(): Unit = {
    for (sem <- sems) {
      sem.dumpAll()
    }
  }




}

object SMItem {
  def apply(projectMD: ProjectMD, cpg: Cpg): SMItem = {
    val smItem = new SMItem(projectMD, ListBuffer.empty)
    val innerSem = InnerFuncSem(projectMD, cpg)
    smItem.appendSem(innerSem)
    val interSem = InterFuncSem(projectMD, cpg)
    smItem.appendSem(interSem)
    val cVarSem = CVarSem(projectMD, cpg)
    smItem.appendSem(cVarSem)
    smItem
  }
}
