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

  def getInterestingFuncs: List[Method] = {
    logger.info(s"==> analyzing ${projectMD}")
    cpg.method.internal.where(m => !MethodWrapper.isIgnoredMethod(m)).l
  }

  override def dumpAll(): Unit = {
    println(s"=== inter info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = ???
}