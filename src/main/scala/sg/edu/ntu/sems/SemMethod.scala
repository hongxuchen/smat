package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.generated.nodes
import io.shiftleft.codepropertygraph.generated.nodes.{ControlStructure, Method}
import io.shiftleft.semanticcpg.dotgenerator.Shared.Edge
import sg.edu.ntu.TypeDefs.MetricsTy

class SpecialCall(m: Method) {

  val stdlibCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.stdlibCalls)

  val kernelUserCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxKernlUserCalls)

  val syscallsCallees: Set[String] = MethodWrapper.specialCallees(m, Utils.linuxSyscalls)

}

case class SemMethod(m: Method,
                     // text
                     slocOpt: Option[Int],
                     // cfg
                     cfgEdges: List[Edge], cfgNodes: List[nodes.CfgNode],
                     controls: List[ControlStructure], branches: List[ControlStructure],
                     loops: List[ControlStructure], loopDepth: Int,
                     // cg
                     isRecursive: Boolean, icallees: Set[Method], icallers: Set[Method],
                     sCall: SpecialCall)
  extends Ordered[SemMethod] {

  import sg.edu.ntu.Config._

  // [0, RecursiveF]
  private val recM: MetricsTy = isRecursive.compare(false) * RecursiveF
  // [0, SLocMax/SLocF]
  private val slocM: MetricsTy = slocOpt match {
    case Some(sloc) => Math.min(sloc, SLocMax) / SLocF
    case None => SLocMax / SLocF
  }
  // [0, CcMax]
  private val ccM: MetricsTy = {
    val cc = cfgEdges.length - cfgNodes.length + 2
    Math.min(cc, CcMax)
  }
  // [0, CfgEdgeM/CfgEdgeW]
  private val cfgEdgesM: MetricsTy = Math.min(cfgEdges.length, CfgEdgeMax) / CfgEdgeF
  // [0, CfgNodeMax/CfgNodeF]
  private val cfgNodesM: MetricsTy = Math.min(cfgNodes.length, CfgNodeMax) / CfgNodeF
  // [0, ICalleeMax]
  private val icalleesM: MetricsTy = Math.min(icallees.size, ICalleeMax)
  // [0, ICallerMax]
  private val icallersM: MetricsTy = Math.min(icallers.size, ICallerMax)
  // [0, ControlMax/ControlF]
  private val controlsM: MetricsTy = Math.min(controls.length, ControlMax) / ControlF
  // [0, BranchMax/BranchF]
  private val branchesM: MetricsTy = Math.min(branches.length, BranchMax) / BranchF
  // [0, LoopM/LoopF]
  private val loopsM: MetricsTy = Math.min(loops.length, LoopMax) / LoopF
  // [0, DeepLoopMax/DeepLoopF]
  private val deepLoopM: MetricsTy = Math.min(loopDepth, DeepLoopMax) / DeepLoopF
  // [0, 10]
  private val specialCallsM: MetricsTy = {
    val size = (sCall.syscallsCallees.size + sCall.stdlibCallees.size + sCall.kernelUserCallees.size)
    Math.min(size, SpecialCallMax) / SpecialCallF
  }


  /**
    * @return a value [0, 100] which indicates a value for the method's feature, for sorting purpose
    */
  private val featureValue: MetricsTy = {
    recM + slocM + ccM + cfgEdgesM + cfgNodesM + icalleesM + icallersM + controlsM + branchesM + deepLoopM + specialCallsM
  }

  override def compare(that: SemMethod): Int = {
    ((that.featureValue - this.featureValue) * PRECISION).toInt
  }

  def asMethodFeatures: Array[MetricsTy] = {
    Array(slocM,
      cfgEdgesM, cfgNodesM, ccM, branchesM, controlsM, loopsM, deepLoopM,
      recM, icalleesM, icallersM, specialCallsM)
  }

}

object SemMethod {

  import MethodWrapper._

  def apply(m: Method): SemMethod = {
    def filter(m: Method) = true

    SemMethod(m,
      // text
      sloc(m),
      // cfg
      getMethodCfgEdges(m),
      getMethodCfgNodes(m),
      getControlStructs(m),
      getBranchStructs(m),
      getLoopStructs(m),
      getLoopDepths(m),
      // calling
      isSelfRecursive(m),
      getICallees(m, filter),
      getICallers(m, filter),
      new SpecialCall(m)
    )
  }
}