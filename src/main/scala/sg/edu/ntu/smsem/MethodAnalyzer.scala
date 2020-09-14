package sg.edu.ntu.smsem

import java.nio.file.Paths

import io.shiftleft.codepropertygraph.generated.{NodeKeysOdb, nodes}
import io.shiftleft.codepropertygraph.generated.nodes.{Call, Method}
import io.shiftleft.dataflowengineoss.language.toTrackingPoint
import io.shiftleft.semanticcpg.dotgenerator.Shared
import io.shiftleft.semanticcpg.dotgenerator.Shared.Edge
import io.shiftleft.semanticcpg.language.{BaseNodeTypeDeco, NoResolve, toAstNodeMethods}
import overflowdb.traversal.jIteratortoTraversal
import overflowdb.{Node, OdbGraph}

import scala.jdk.CollectionConverters._

object MethodAnalyzer {

  def callOutsAreConst(method: Method): Boolean = {
    method.start.call.callee(NoResolve).internal.l.forall(_.signature.contains("const"))
  }

  def parameterOpsAreConst(method: Method): Boolean = {
    method.start.parameter.inAssignment.source.l.forall {
      case c: Call => c.signature.contains("const")
      case _ => false
    } && method.start.assignments.target.reachableBy(method.start.parameter).isEmpty
  }

  def callees(method: Method, funcs: Set[String]): Set[String] = {
    method.start.call.callee(NoResolve).name.where(n => funcs.contains(n)).toSet
  }

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
      case e: Exception => {
        logger.warn(s"${e.getMessage}")
        ""
      }
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