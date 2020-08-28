package sg.edu.ntu.smsem

import java.nio.file.Paths

import io.shiftleft.codepropertygraph.generated.{EdgeTypes, NodeTypes, nodes}
import io.shiftleft.codepropertygraph.generated.nodes.{Block, Call, ControlStructure, Expression, Method, Return}
import org.apache.tinkerpop.gremlin.structure.Vertex
import overflowdb.Node
import overflowdb._
import overflowdb.traversal._
import io.shiftleft.Implicits.JavaIteratorDeco
import io.shiftleft.codepropertygraph.generated._

import scala.annotation.tailrec

object MethodAnalyzer {
  def getTopLevelExpressions(expression: Node): List[Expression] = {
    expression
      .out(EdgeTypes.AST)
      .hasLabel(NodeTypes.BLOCK, NodeTypes.CONTROL_STRUCTURE, NodeTypes.RETURN, NodeTypes.CALL)
      .cast[nodes.Expression]
      .l
  }

//  def dotFromMethod(method: Method): List[String] = {
//    @tailrec
//    def go(subExprs: List[Expression],
//           parent: Option[Expression] = None,
//           dots: List[String] = List.empty): List[String] = {
//
//      val parentId = parent.map(_.id.toString).getOrElse(method.id)
//
//      subExprs match {
//        case Nil =>
//          dots
//        case expr :: tail => expr match {
//          case ex: Block =>
//            val currDotRepr = s""" "$parentId" -> "${ex.id}" [label=BLOCK];"""
//            go(MethodAnalyzer.getTopLevelExpressions(ex) ::: tail, Some(ex), dots :+ currDotRepr)
//          case ex: ControlStructure =>
//            val currDotRepr = s""" "$parentId" -> "${ex.id}" [label="${ex.code}"];"""
//            go(MethodAnalyzer.getTopLevelExpressions(ex) ::: tail, Some(ex), dots :+ currDotRepr)
//          case ex: Return =>
//            val currDotRepr = s""" "$parentId" -> "${ex.id}" [label="${ex.code}"];"""
//            go(tail, parent, dots :+ currDotRepr)
//          case ex: Call =>
//            val currDotRepr = s""" "$parentId" -> "${ex.id}" [label="${ex.code}"];"""
//            go(tail, parent, dots :+ currDotRepr)
//          case _ =>
//            // Ignore all other node types.
//            go(tail, parent, dots)
//        }
//      }
//    }
//
//    val methodExpressions = method
//      .asInstanceOf[Vertex] //TODO MP drop as soon as we have the remainder of the below in ODB graph api
//      .out(EdgeTypes.AST)
//      .hasLabel(NodeTypes.BLOCK)
//      .out(EdgeTypes.AST)
//      .not(_.hasLabel(NodeTypes.LOCAL, NodeTypes.TYPE_DECL))
//      .cast[nodes.Expression]
//      .l
//
//    go(methodExpressions)
//  }

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
}

class MethodAnalyzer(method: Method) {

}

final class InnerFuncSem extends SMSem {

}
