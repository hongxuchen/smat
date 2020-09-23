package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language._
import sg.edu.ntu.ProjectMD

import scala.collection.mutable.ListBuffer

class MethodSem(m: Method) {

  val stdlibCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.stdlibCalls)

  val kernelUserCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxKernlUserCalls)

  val syscallsCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxSyscalls)

}

final case class InnerFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val features:ListBuffer[FeatureTy] = ListBuffer.empty

  val methodList: List[Method] = cpg.method.l

  val methodSems: ListBuffer[MethodSem] = ListBuffer.empty

  def ccComplexity(m: Method): Int = {
    val edges = MethodWrapper.getMethodCfgEdges(m)
    val nodes = MethodWrapper.getMethodCfgNodes(m)
    edges.length - nodes.length + 2
  }

  def dumpInfo(m: Method, index: Option[Int]): Unit = {

    m.start.call.parameter(NoResolve)

    //    m.start.ast.isIdentifier
    //    m.start.call.code()
    //    m.start.controlStructure
    //    m.start.call.name("source").file
    //    m.start.call.inAssignment
  }

  def dumpAll(): Unit = {
    println(s"=== inner info for ${projectMD} ===")
    for ((m, i) <- methodList.zipWithIndex) {
      dumpInfo(m, Some(i))
    }
  }

  override def collectFeatures(): Unit = {

  }
}
