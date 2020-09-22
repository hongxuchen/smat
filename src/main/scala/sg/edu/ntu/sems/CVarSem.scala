package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.{Call, Literal, _}
import io.shiftleft.semanticcpg.language.BaseNodeTypeDeco
import sg.edu.ntu.ProjectMD

import scala.collection.mutable.ListBuffer

final case class CVarSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val features: ListBuffer[FeatureTy] = ListBuffer.empty

  override def dumpAll(): Unit = {
    println(s"=== const info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = ???
}

object CVarSem {

  def getConstActualParams(call: Call, filter: Literal => Boolean = _ => true): List[(Literal, Integer)] = {

    call.start.argument.map { argument =>
      argument match {
        case id: Identifier => {

        }
        case literal: Literal => {
          if (filter(literal)) {
          }
        }
      }
    }

    call.start.argument.isLiteral.where(filter).map { l =>
      (l, l.order)
    }.l
  }

}