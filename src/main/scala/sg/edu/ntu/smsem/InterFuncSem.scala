package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language.toNodeTypeStarters
import sg.edu.ntu.ProjectMD

/**
  * this class stores the coarse grained features across functions
  * finally the semantic is a 1-dimention vector
  * @param projectMD
  * @param cpg
  */
final case class InterFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  def getFuncs: List[Method] = {
    logger.info(s"==> analyzing ${projectMD}")
    val methods = cpg.method.l
    println(methods.size)
    for (method <- methods) {
      val dotStr = MethodAnalyzer.toDot(method.graph())
      println(dotStr)
    }
    val methodList = cpg.method.l
    for (method <- methodList) {
      println(s"${method}\t")
    }
    val callList = cpg.call.l()
    for (call <- callList) {
      println(s"${call.name}")
    }
    methodList
  }

  override def dumpAll(): Unit = {
    println(s"=== inter info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = ???
}