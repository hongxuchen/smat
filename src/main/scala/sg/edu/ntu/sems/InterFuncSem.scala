package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.generated.nodes.Method
import sg.edu.ntu.TypeDefs.ScoreTy
import sg.edu.ntu.matching.Similarity
import sg.edu.ntu.{Config, ModuleMD}

import scala.collection.mutable

/**
  * this class stores the coarse grained features across functions
  * finally the semantic is a 1-dimention vector
  *
  * @param moduleMD
  * @param smms
  */
final case class InterFuncSem(moduleMD: ModuleMD, methods: List[Method],
                              smms: List[SemMethod]) extends SMSem {

  val stdlib: Set[String] = _getCallees(_.stdlibCallees)
  val sys: Set[String] = _getCallees(_.syscallsCallees)
  val kernel: Set[String] = _getCallees(_.kernelUserCallees)

  private val allMethodNum: Int = methods.length
  private val semMethodNum: Int = smms.length
  private val recursiveMethodNum: Int = smms.count(_.isRecursive)
  private val icallees: Int = smms.map(_.icallees.size).sum
  private val icallers: Int = smms.map(_.icallers.size).sum
  private val deepLoopMethodNum: Int = smms.count(_.loopDepth > Config.LoopSmallMax)
  private val methodFileNum: Int = smms.map(_.m.filename).toSet.size

  def _getCallees(f: SpecialCall => Set[String]): Set[String] = {
    val s: mutable.Set[String] = mutable.Set.empty
    smms.foldLeft(s)((acc, cur) => acc ++= f(cur.sCall))
    s.toSet
  }

  val miscFeatures: Array[Double] = {
    Array(
      // TODO unit test for them
      icallees.toDouble / (icallees + icallers).toDouble,
      icallers.toDouble / (icallees + icallers).toDouble,
      semMethodNum.toDouble / allMethodNum.toDouble,
      deepLoopMethodNum.toDouble / semMethodNum.toDouble,
      recursiveMethodNum.compare(0)
    )
  }

  override def dumpAll(): Unit = {
    println(s"=== inter info for ${moduleMD} ===")
  }

  override def calculateSim(other: InterFuncSem.this.type): ScoreTy = {
    val s1 = {
      val sl = Array(
        Similarity.getJaccardSim(this.stdlib, other.stdlib),
        Similarity.getJaccardSim(this.kernel, other.kernel),
        Similarity.getJaccardSim(this.sys, other.sys),
      )
      sl.sum / sl.length
    }
    val s2 = Similarity.getCosineSim(miscFeatures, other.miscFeatures)
    (s1 + s2) / 2.0
  }
}