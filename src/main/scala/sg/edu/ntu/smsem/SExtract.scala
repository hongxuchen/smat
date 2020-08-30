package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD

object SExtract {

  def analyze(projectMD: ProjectMD, cpg: Cpg): Unit = {
    var smItem = SMItem(projectMD, List.empty)
    val interFuncSem = new InterFuncSem(projectMD, cpg)
    smItem.appendSem(interFuncSem)
    interFuncSem.getFuncs
    val dotStr = MethodAnalyzer.toDot(cpg.graph)
    println(dotStr)
  }

}
