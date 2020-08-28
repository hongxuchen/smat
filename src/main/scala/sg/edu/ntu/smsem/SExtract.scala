package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD

object SExtract {

  def analyze(projectMD: ProjectMD, cpg: Cpg): Unit = {
    val smItem = SMItem(projectMD, List.empty)
    val interFuncSem = new InterFuncSem(projectMD, cpg)
    interFuncSem.getFuncs
    val dotStr = MethodAnalyzer.toDot(cpg.graph)
    println(dotStr)
  }

}
