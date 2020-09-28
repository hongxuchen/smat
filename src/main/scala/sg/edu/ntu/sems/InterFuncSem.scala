package sg.edu.ntu.sems

import sg.edu.ntu.ProjectMD
import sg.edu.ntu.matching.{ScoreTy, Similarity}

import scala.collection.mutable

/**
  * this class stores the coarse grained features across functions
  * finally the semantic is a 1-dimention vector
  *
  * @param projectMD
  * @param smms
  */
final case class InterFuncSem(projectMD: ProjectMD, smms: List[SemMethod]) extends SMSem {

  val stdlib: Set[String] = _getCallees(_.stdlibCallees)
  val sys: Set[String] = _getCallees(_.syscallsCallees)
  val kernel: Set[String] = _getCallees(_.kernelUserCallees)

  val features:Array[MetricsTy] = {

  }

  def _getCallees(f: SpecialCall => Set[String]): Set[String] = {
    val s: mutable.Set[String] = mutable.Set.empty
    smms.foldLeft(s)((acc, cur) => acc ++= f(cur.sCall))
    s.toSet
  }

  override def dumpAll(): Unit = {
    println(s"=== inter info for ${projectMD} ===")
  }

  override def calculateSim(other: InterFuncSem.this.type): ScoreTy = {
    val s = List(
      Similarity.getJaccardSim(this.stdlib, other.stdlib),
      Similarity.getJaccardSim(this.kernel, other.kernel),
      Similarity.getJaccardSim(this.sys, other.sys)
    )
    s.sum / s.size.toDouble
  }
}