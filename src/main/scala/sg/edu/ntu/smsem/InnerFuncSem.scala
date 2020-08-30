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
