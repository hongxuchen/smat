package sg.edu.ntu.sems


import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes
import io.shiftleft.codepropertygraph.generated.nodes.{ControlStructure, Method}
import io.shiftleft.semanticcpg.dotgenerator.Shared.Edge
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.TypeDefs.MetricsTy
import sg.edu.ntu.matching.ScoredProj

import scala.collection.mutable.ListBuffer

class SpecialCall(m: Method) {

  val stdlibCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.stdlibCalls)

  val kernelUserCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxKernlUserCalls)

  val syscallsCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxSyscalls)

}

case class SemMethod(m: Method,
                     isRecursive: Boolean, slocOpt: Option[Int],
                     cfgEdges: List[Edge], cfgNodes: List[nodes.CfgNode],
                     icallees: List[Method], icallers: List[Method],
                     controls: List[ControlStructure], branches: List[ControlStructure],
                     loops: List[ControlStructure], loopDepth: Int,
                     sCall: SpecialCall)
  extends Ordered[SemMethod] {

  import sg.edu.ntu.Config._

  // [0, RecursiveF]
  private val recursiveM: Int = isRecursive.compare(false) * RecursiveF
  // [0, SLocMax/SLocF]
  private val slocM: Int = slocOpt match {
    case Some(sloc) => Math.min(sloc, SLocMax) / SLocF
    case None => SLocMax / SLocF
  }
  // [0, CcMax]
  private val ccM: Int = {
    val cc = cfgEdges.length - cfgNodes.length + 2
    Math.min(cc, CcMax)
  }
  // [0, CfgEdgeM/CfgEdgeW]
  private val cfgEdgesM: Int = Math.min(cfgEdges.length, CfgEdgeMax) / CfgEdgeF
  // [0, CfgNodeMax/CfgNodeF]
  private val cfgNodeM: Int = Math.min(cfgNodes.length, CfgNodeMax) / CfgNodeF
  // [0, ICalleeMax]
  private val icalleesM: Int = Math.min(icallees.length, ICalleeMax)
  // [0, ICallerMax]
  private val icallerM: Int = Math.min(icallers.length, ICallerMax)
  // [0, ControlMax/ControlF]
  private val controlsM: Int = Math.min(controls.length, ControlMax) / ControlF
  // [0, BranchMax/BranchF]
  private val branchesM: Int = Math.min(branches.length, BranchMax) / BranchF
  // [0, DeepLoopMax/DeepLoopF]
  private val deepLoopM: Int = Math.min(loopDepth, DeepLoopMax) / DeepLoopF
  // [0, 10]
  private val specialCallM: Int = {
    val size = (sCall.syscallsCallees.size + sCall.stdlibCallees.size + sCall.kernelUserCallees.size)
    Math.min(size, SpecialCallMax) / SpecialCallF
  }


  /**
    * @return a value [0, 100] which indicates a value for the method's feature, for sorting purpose
    */
  private val featureValue: Int = {
    recursiveM + slocM + ccM + cfgEdgesM + cfgNodeM + icalleesM + icallerM + controlsM + branchesM + deepLoopM + specialCallM
  }

  override def compare(that: SemMethod): Int = {
    that.featureValue - this.featureValue
  }

  def asMethodFeatures: Array[MetricsTy] = {
    Array(recursiveM, slocM,
      ccM, cfgEdgesM, cfgNodeM,
      icalleesM, icallerM,
      controlsM, branchesM, deepLoopM, specialCallM)
  }

}

object SemMethod {

  import MethodWrapper._

  def apply(m: Method): SemMethod = {
    def filter(m: Method) = true

    SemMethod(m,
      isSelfRecursive(m),
      sloc(m),
      getMethodCfgEdges(m),
      getMethodCfgNodes(m),
      getICallees(m, filter),
      getICallers(m, filter),
      getControlStructs(m),
      getBranchStructs(m),
      getLoopStructs(m),
      getLoopDepths(m),
      new SpecialCall(m)
    )
  }
}

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

  import CpgWrapper._

  def getSemMethods(cpg: Cpg): List[SemMethod] = {
    val methods: List[Method] = getInterestingFuncs(cpg)
    methods.map { m => SemMethod(m) }.sorted
  }

  /**
    * instance factory for `SMItem`, with all the semantic fields
    *
    * @param projectMD
    * @param cpg
    * @return
    */
  def apply(projectMD: ProjectMD, cpg: Cpg): SMItem = {
    val semMethods = getSemMethods(cpg)
    val smItem = new SMItem(projectMD, ListBuffer.empty)
    val magicSem = MagicSem(projectMD, cpg, semMethods)
    smItem.appendSem(magicSem)
    val interSem = InterFuncSem(projectMD, getAllFuncs(cpg), semMethods)
    smItem.appendSem(interSem)
    val innerSem = InnerFuncSem(projectMD, semMethods)
    smItem.appendSem(innerSem)
    smItem
  }
}
