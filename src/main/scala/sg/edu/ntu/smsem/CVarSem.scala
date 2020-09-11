package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD

import scala.collection.mutable.ListBuffer

final case class CVarSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val features: ListBuffer[FeatureTy] = ListBuffer.empty

  override def dumpAll(): Unit = {
    println(s"=== const info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = ???
}
