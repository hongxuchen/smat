package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language._
import sg.edu.ntu.ProjectMD

/**
  * this class stores the coarse grained features across functions
  * finally the semantic is a 1-dimention vector
  *
  * @param projectMD
  * @param cpg
  */
final case class InterFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  def getConstFuncs: List[Method] = {
    cpg.method.internal.where { m =>
      !m.signature.contains("const") && MethodWrapper.callOutsAreConst(m) && MethodWrapper.parameterOpsAreConst(m)
    }.toList()
  }

  def getFuncs: List[Method] = {
    logger.info(s"==> analyzing ${projectMD}")
    val methods = cpg.method.l
    println(methods.size)
    for (method <- methods) {
      val dotStr = MethodWrapper.toDot(method.graph())
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