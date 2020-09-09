package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD

final case class CVarSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {
  override def dumpAll(): Unit = {
    println(s"=== const info for ${projectMD} ===")
  }
}
