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

  def isSmallFunc(m: Method): Boolean = {
    MethodWrapper.loc(m) match {
      case Some(line) => line < Utils.LOC_THRESHOD
      case _ => false
    }
  }

  def isSelfRecursive(m: Method): Boolean = {
    false
  }

  def isInternal(m: Method): Boolean = {
    m.name.startsWith("_")
  }

  def isIgnoredMethod(m: Method): Boolean = {
    !isSelfRecursive(m) && isInternal(m) || isSmallFunc(m)
  }


  def getFuncs: List[Method] = {
    logger.info(s"==> analyzing ${projectMD}")
    val definedMethods = cpg.method.internal.l

    for (method <- definedMethods) {
      val dotStr = MethodWrapper.astStr(method)
      println(dotStr)
    }
    val callList = cpg.call.l()
    for (call <- callList) {
      println(s"${call.name}")
    }
    ???
  }

  override def dumpAll(): Unit = {
    println(s"=== inter info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = ???
}