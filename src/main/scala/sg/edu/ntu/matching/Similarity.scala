package sg.edu.ntu.matching

import sg.edu.ntu.TypeDefs.ScoreTy
import sg.edu.ntu.{Config, ModuleMD}
import smile.math.MathEx
import smile.math.distance.JaccardDistance

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.math.Ordering.Double

object Similarity {

  def getJaccardSim[A](sa: Set[A], sb: Set[A]): Double = {
    JaccardDistance.d(sa.asJava, sb.asJava)
  }

  def getCosineSim(aa: Array[Double], ab: Array[Double]): Double = {
    MathEx.cos(aa, ab)
  }

  //  def getCosineSim(aa: Array[MetricsTy], ab: Array[MetricsTy]): Double = {
  //    getCosineSim(aa.map(_.toDouble), ab.map(_.toDouble))
  //  }

}

case class ScoredMod(moduleMD: ModuleMD, scores: List[ScoreTy])

object Scoring {

  def thresholdGen(len: Int): List[ScoreTy] = {
    val d = Range(1, len + 1).foldLeft(0.0d)((acc, cur) => acc + 1.0 / cur.toDouble)
    Range(1, len + 1).map { cur => 1.0 / cur / d }.toList
  }

  def inverseWeightGen(len: Int): List[ScoreTy] = {
    val d: Double = (len + 1) * len / 2
    (len to 1 by -1).map { cur: Int => cur.toDouble / d }.toList
  }

  def gridThresholdGen(len: Int, base: ScoreTy = 0.0, modifier: Int = 2): List[ScoreTy] = {
    val diff = (1.0 - base) / (len + modifier)
    (len to 1 by -1).map(i => i * diff + base).toList
  }

  def getLen(sps: List[ScoredMod]): Int = {
    require(sps.nonEmpty)
    val _len = sps.head.scores.length
    sps.foreach { sp =>
      require(sp.scores.length == _len)
    }
    _len
  }

}

sealed trait Scoring {

  implicit val dOrdering: Double.IeeeOrdering.type = Ordering.Double.IeeeOrdering

  def sps: List[ScoredMod]

  protected def sortedMods: List[ModuleMD]

  def getTops(n: Int = Config.ScoreTops): List[ModuleMD] = sortedMods.take(n)

}

final case class NDimScoring(sps: List[ScoredMod]) extends Scoring {


  @tailrec
  def greaterOrEq(sl1: List[ScoreTy], sl2: List[ScoreTy]): Boolean = {
    (sl1, sl2) match {
      case (sl1l :: Nil, sl2l :: Nil) => {
        sl1l >= sl2l
      }
      case (sl1h :: sl1t, sl2h :: sl2t) => {
        if (sl1h > sl2h) true else greaterOrEq(sl1t, sl2t)
      }
      case (_, _) => {
        throw new RuntimeException("shouldn't happen")
      }
    }
  }

  override def sortedMods: List[ModuleMD] = {
    sps.sortWith { case (sp1: ScoredMod, sp2: ScoredMod) => {
      greaterOrEq(sp1.scores, sp2.scores)
    }
    }.map(_.moduleMD)
  }
}

final case class ThresholdScoring(sps: List[ScoredMod]) extends Scoring {

  val len: Int = Scoring.getLen(sps)

  /**
    * tail recursive sorting according to threshold in each dimention, note that as long as threthold matches we ignore remaining dimention values
    *
    * @param sorted
    * @param remain
    * @param thresholds
    * @return
    */
  @tailrec
  def thresholdMatched(sorted: ListBuffer[ScoredMod], remain: ListBuffer[ScoredMod], thresholds: List[ScoreTy]): ListBuffer[ModuleMD] = {
    thresholds match {
      case Nil => {
        val sortIndex = len - 1
        sorted ++= remain.sortBy(_.scores(sortIndex))
//        println(s"*** ${sorted}")
        sorted.map(_.moduleMD)
      }
      case cur :: otherThresholds => {
        val sortIndex = len - 1 - otherThresholds.length
        val (toSort, newRemain) = remain.partition(x => {
//          println(s"== ${x.scores}, ${x.scores(sortIndex)}, ${cur}")
          x.scores(sortIndex) > cur
        })
        println(sortIndex, cur, toSort, newRemain)
        sorted ++= toSort.sortBy(_.scores(sortIndex))
        thresholdMatched(sorted, newRemain, otherThresholds)
      }
    }
  }

  override def sortedMods: List[ModuleMD] = {
    val thresholds = Scoring.gridThresholdGen(len)
    thresholdMatched(ListBuffer.empty, sps.to(ListBuffer), thresholds).toList
  }
}

/**
  * weighted scoring approach
  * @param sps
  */
final case class WeightedScoring(sps: List[ScoredMod]) extends Scoring {

  val len: Int = Scoring.getLen(sps)

  def overallScore(sl: List[ScoreTy], wl: List[ScoreTy]): ScoreTy = {
    sl.zip(wl).map { case (s, w) => s * w }.sum
  }

  override def sortedMods: List[ModuleMD] = {
    val weights = Scoring.inverseWeightGen(len)
    sps.map { sp =>
      require(sp.scores.length == len)
      sp.moduleMD -> overallScore(sp.scores, weights)
    }.sortBy(-_._2).map(_._1)
  }
}

