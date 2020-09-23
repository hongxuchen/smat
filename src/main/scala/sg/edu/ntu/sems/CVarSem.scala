package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.{Call, Literal, _}
import io.shiftleft.semanticcpg.language.BaseNodeTypeDeco
import sg.edu.ntu.ProjectMD

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

final case class CVarSem(projectMD: ProjectMD, cpg: Cpg) extends SMSem {

  val features: ListBuffer[FeatureTy] = ListBuffer.empty

  override def dumpAll(): Unit = {
    println(s"=== const info for ${projectMD} ===")
  }

  override def collectFeatures(): Unit = {

  }
}

object CVarSem {

  def getSpecialArgs(call: Call, filter: Literal => Boolean = _ => true): (mutable.Set[Literal], mutable.Set[Identifier]) = {

    val literals: mutable.Set[Literal] = mutable.Set.empty
    val identifiers: mutable.Set[Identifier] = mutable.Set.empty

    call.start.argument.map {
      case id: Identifier => {
        if (id.name.toUpperCase == id.name) {
          identifiers.add(id)
        }
      }
      case literal: Literal => {
        if (filter(literal)) {
          literals.add(literal)
        }
      }
    }
    (literals, identifiers)
  }

  def getUniqueLiterals(literals: mutable.Set[Literal]): Set[Literal] = {
    def literalKey(l: Literal): Int = {
      l.code.hashCode + 3 * l.typeFullName.hashCode
    }

    literals.map(l => (literalKey(l), l)).toMap.values.toSet
  }

  def getUniqueIdentifiers(identifiers: mutable.Set[Identifier]): Set[Identifier] = {
    def identifierKey(id: Identifier): Int = {
      id.name.hashCode + 3 * id.typeFullName.hashCode + id.label.hashCode
    }

    identifiers.map(id => (identifierKey(id), id)).toMap.values.toSet
  }

}