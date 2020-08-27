package sg.edu.ntu

import io.shiftleft.codepropertygraph.Cpg
import org.slf4j.LoggerFactory

package object smsem {

  case class SMInfo(projectMD: ProjectMD, cpg: Cpg)

  abstract class SMSem() {
  }

  val logger = LoggerFactory.getLogger(this.getClass)

}
