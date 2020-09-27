package sg.edu.ntu.matching

import sg.edu.ntu.sems.SMItem
import smile.math.MathEx
import smile.math.distance.JaccardDistance

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

object Similarity {

  type CosEleTy = Double

  def getJaccardSim[A](sa: Set[A], sb: Set[A]): Double = {
    JaccardDistance.d(sa.asJava, sb.asJava)
  }

  def getCosineSim(la: ListBuffer[CosEleTy], lb: Iterator[CosEleTy]): Double = {
    val aa: Array[CosEleTy] = la.toArray
    val ab: Array[CosEleTy] = la.toArray
    MathEx.cos(aa, ab)
  }

  def getSemSimilarities(si1: SMItem, si2: SMItem): List[ScoreTy] = {
    require(si1.sems.length == si2.sems.length)
    //    si1.sems.zip(si2.sems).map { case (sm1, sm2) => {
    //      case (s1: InterFuncSem, s2: InterFuncSem) => {
    //
    //      }
    //      case (s1: InnerFuncSem, s2: InnerFuncSem) => {
    //
    //      }
    //      case (s1: CVarSem, s2: CVarSem) => {
    //
    //      }
    //      case _ => {
    //        ???
    //      }
    //    }
    //
    //    }
    ???
  }

}
