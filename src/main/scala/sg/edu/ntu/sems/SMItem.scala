package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language.toNodeTypeStarters
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.matching.ScoredProj

import scala.collection.mutable.ListBuffer

class CalleesSM(m: Method) {

  val stdlibCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.stdlibCalls)

  val kernelUserCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxKernlUserCalls)

  val syscallsCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxSyscalls)

}

case class SemMethod(m: Method, calleesSM: CalleesSM)

@SerialVersionUID(100L)
case class SMItem(projectMD: ProjectMD, sems: ListBuffer[SMSem]) extends Serializable {

  def appendSem(sem: SMSem): Unit = {
    logger.info(s"adding ${sem.getClass} to ${projectMD}")
    sems.append(sem)
  }

  def dump(): Unit = {
    for (sem <- sems) {
      sem.dumpAll()
    }
  }

  def calculateSim(other: SMItem): ScoredProj = {
    val scores = this.sems.zip(other.sems).map { case (sm1, sm2) => {
      sm1.calculateSim(sm2.asInstanceOf[sm1.type])
    }
    }.toList
    ScoredProj(other.projectMD, scores)
  }
}

object SMItem {

  def getInterestingFuncs(cpg: Cpg): List[Method] = {
    cpg.method.internal.where(m => !MethodWrapper.isIgnoredMethod(m)).l
  }

  def getConstFuncs(cpg: Cpg): List[Method] = {
    cpg.method.internal.where { m =>
      !m.signature.contains("const") && MethodWrapper.callOutsAreConst(m) && MethodWrapper.parameterOpsAreConst(m)
    }.toList()

  }

  /**
    * instance factory for `SMItem`, with all the semantic fields
    *
    * @param projectMD
    * @param cpg
    * @return
    */
  def apply(projectMD: ProjectMD, cpg: Cpg): SMItem = {
    val semMethods: List[SemMethod] = {
      val methods: List[Method] = getInterestingFuncs(cpg)
      methods.map { m => {
        val specialCallees = new CalleesSM(m)
        SemMethod(m, specialCallees)
      }
      }
    }
    val smItem = new SMItem(projectMD, ListBuffer.empty)
    val magicSem = MagicSem(projectMD, cpg)
    smItem.appendSem(magicSem)
    val interSem = InterFuncSem(projectMD, semMethods)
    smItem.appendSem(interSem)
    val innerSem = InnerFuncSem(projectMD, semMethods)
    smItem.appendSem(innerSem)
    smItem
  }
}
