package sg.edu.ntu.smsem

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.dotgenerator.{DotCfgGenerator, Shared}
import io.shiftleft.semanticcpg.language._
import sg.edu.ntu.ProjectMD

import scala.collection.mutable.ListBuffer

class MethodSem(m: Method) {

  val stdlibCallees: Set[String] = MethodAnalyzer.callees(m, Utils.stdlibCalls)

  val kernelUserCallees: Set[String] = MethodAnalyzer.callees(m, Utils.linuxKernlUserCalls)

  val syscallsCallees: Set[String] = MethodAnalyzer.callees(m, Utils.linuxSyscalls)

}

final case class InnerFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val methodList: List[Method] = cpg.method.l

  val methodSems: ListBuffer[MethodSem] = ListBuffer.empty

  def getCfg(method: Method): String = {
    Shared.dotGraph(method, MethodAnalyzer.expandedStoredNodes, DotCfgGenerator.cfgNodeShouldBeDisplayed)
  }

  def getAst(method: Method): String = {
    val sb = Shared.namedGraphBegin(method)
    sb.append(MethodAnalyzer.nodesAndEdges(method).mkString("\n"))
    Shared.graphEnd(sb)
  }

  def dumpInfo(m: Method, index: Option[Int]): Unit = {

    println(s"${optStr(index, "")} ${m.fullName}: isExernal:${m.isExternal}," +
      s" sig: ${m.signature}, " +
      s"pos: [(${optStr(m.lineNumber)},${optStr(m.columnNumber)}),(${optStr(m.lineNumberEnd)}, ${optStr(m.columnNumberEnd)})]")
    //    val methodSteps = new Steps(m)
    //    dumpToFile(getCfg(m), projectMD.toString, m.fullName, "cfg")
    //    dumpToFile(getAst(m), projectMD.toString, m.fullName, "ast")
    //    println(s"cfg:\n${getCfg(m)}\nast:\n${getAst(m)}")
  }

  def dumpAll(): Unit = {
    println(s"=== inner info for ${projectMD} ===")
    for ((m, i) <- methodList.zipWithIndex) {
      dumpInfo(m, Some(i))
    }
  }

  override def collectFeatures(): Unit = ???
}
