package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language.toNodeTypeStarters
import sg.edu.ntu.ProjectMD

final class InterFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  def getFuncs: List[Method] = {
    val methodList = cpg.method.l
    println(s"${projectMD}")
    for (method <- methodList) {
      print(s"${method}\t")
    }
    methodList
  }

}
