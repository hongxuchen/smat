package sg.edu.ntu.smsem

import java.nio.file.Paths

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.{nodes, _}
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.dotgenerator.{DotCfgGenerator, Shared}
import io.shiftleft.semanticcpg.dotgenerator.Shared.Edge
import io.shiftleft.semanticcpg.language._
import overflowdb.{Node, _}
import overflowdb.traversal._
import sg.edu.ntu.ProjectMD

import scala.jdk.CollectionConverters._

object MethodAnalyzer {

  /** Some helper functions: adapted from ReachingDefPass.scala in codeproperty graph repo */
  def vertexToStr(vertex: Node): String = {
    try {
      val methodVertex = vertex.in("CONTAINS").next
      val fileName = methodVertex.in("CONTAINS").next match {
        case file: nodes.File => file.asInstanceOf[nodes.File].name
        case _ => "NA"
      }

      s"${Paths.get(fileName).getFileName.toString}: ${vertex.property(NodeKeysOdb.LINE_NUMBER)} ${vertex.property(NodeKeysOdb.CODE)}"
    } catch {
      case _: Exception => ""
    }
  }

  def toDot(graph: OdbGraph): String = {
    val buf = new StringBuffer()

    buf.append("digraph g {\n node[shape=plaintext];\n")

    graph.edges("CFG").l.foreach { e =>
      val inV = vertexToStr(e.inNode).replace("\"", "\'")
      val outV = vertexToStr(e.outNode).replace("\"", "\'")
      buf.append(s""" "$outV" -> "$inV";\n """)
    }
    buf.append {
      "}"
    }
    buf.toString
  }

  /**
    * taken from `DotCfgGenerator`
    *
    * @param v
    * @return
    */
  def expandedStoredNodes(v: nodes.StoredNode): Iterator[Edge] = {
    v._cfgOut()
      .asScala
      .filter(_.isInstanceOf[nodes.StoredNode])
      .map(node => Edge(v, node))
  }

  /**
    * taken from `DotAstGenerator`
    *
    * @param astRoot
    * @return
    */
  def nodesAndEdges(astRoot: nodes.AstNode): List[String] = {

    def shouldBeDisplayed(v: nodes.AstNode): Boolean = !v.isInstanceOf[nodes.MethodParameterOut]

    val vertices = astRoot.ast.where(shouldBeDisplayed).l
    val edges = vertices.map(v => (v.id2, v.start.astChildren.where(shouldBeDisplayed).id.l))

    val nodeStrings = vertices.map { node =>
      s""""${node.id2}" [label = "${Shared.stringRepr(node)}" ]""".stripMargin
    }

    val edgeStrings = edges.flatMap {
      case (id, childIds) =>
        childIds.map(childId => s"""  "$id" -> "$childId"  """)
    }

    nodeStrings ++ edgeStrings
  }

}


final case class InnerFuncSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val methodList: List[Method] = cpg.method.l

  def getCfg(method: Method): String = {
    Shared.dotGraph(method, MethodAnalyzer.expandedStoredNodes, DotCfgGenerator.cfgNodeShouldBeDisplayed)
  }

  def getAst(method: Method): String = {
    val sb = Shared.namedGraphBegin(method)
    sb.append(MethodAnalyzer.nodesAndEdges(method).mkString("\n"))
    Shared.graphEnd(sb)
  }

  def dumpInfo(m: Method, index: Option[Integer]): Unit = {

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

}
