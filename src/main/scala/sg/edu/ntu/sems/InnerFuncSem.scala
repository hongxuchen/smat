package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language._
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.matching.ScoreTy

import scala.collection.mutable.ListBuffer

final case class InnerFuncSem(projectMD: ProjectMD, smms: List[SemMethod]) extends SMSem {

  val featureList: ListBuffer[Array[MetricsTy]] = ListBuffer.empty

  def dumpInfo(m: Method, index: Option[Int]): Unit = {

    m.start.call.parameter(NoResolve)

    //    m.start.ast.isIdentifier
    //    m.start.call.code()
    //    m.start.controlStructure
    //    m.start.call.name("source").file
    //    m.start.call.inAssignment
  }

  def dumpAll(): Unit = {
    println(s"=== inner info for ${projectMD} ===")
    for ((m, i) <- smms.zipWithIndex) {
      dumpInfo(m.m, Some(i))
    }
  }

  def collectFeatures(): Unit = {

    for (m <- smms) {
      val features: Array[MetricsTy] = Array.empty
      features(0) = m.calleesSM.kernelUserCallees.size
      features(1) = m.calleesSM.stdlibCallees.size
      features(2) = m.calleesSM.syscallsCallees.size
      featureList.addOne(features)
    }
  }

  override def calculateSim(other: InnerFuncSem.this.type): ScoreTy = {
    ???
  }
}

object InnerFuncSem {

}
