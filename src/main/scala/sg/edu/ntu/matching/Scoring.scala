package sg.edu.ntu.matching

import sg.edu.ntu.ProjectMD

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

case class ScoredProj(projectMD: ProjectMD, scores: List[ScoreTy])

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

}


sealed trait Scoring {

  def sortedProjs: List[MatchedTy]

  def getTops(n: Int = 10): List[MatchedTy] = sortedProjs.take(n)

}

final case class NDimScoring(len: Int, sps: List[ScoredProj]) extends Scoring {
  override def sortedProjs: List[MatchedTy] = ???
}

final case class ThresholdScoring(len: Int, sps: List[ScoredProj]) extends Scoring {
  /**
    * tail recursive sorting according to threshold in each dimention, note that as long as threthold matches we ignore remaining dimention values
    *
    * @param sorted
    * @param remain
    * @param thresholds
    * @return
    */
  @tailrec
  def threholdMatched(sorted: ListBuffer[ScoredProj], remain: ListBuffer[ScoredProj], thresholds: List[ScoreTy]): ListBuffer[MatchedTy] = {
    thresholds match {
      case Nil => {
        val sortIndex = len - 1
        sorted ++= remain.sortBy(_.scores(sortIndex))
        println(s"*** ${sorted}")
        sorted.map(_.projectMD)
      }
      case cur :: otherThresholds => {
        val sortIndex = len - 1 - otherThresholds.length
        val (toSort, newRemain) = remain.partition(x => {
          println(s"== ${x.scores}, ${x.scores(sortIndex)}, ${cur}")
          x.scores(sortIndex) > cur
        })
        println(sortIndex, cur, toSort, newRemain)
        sorted ++= toSort.sortBy(_.scores(sortIndex))
        threholdMatched(sorted, newRemain, otherThresholds)
      }
    }
  }

  override def sortedProjs: List[MatchedTy] = {
    val thresholds = Scoring.gridThresholdGen(len)
    threholdMatched(ListBuffer.empty, sps.to(ListBuffer), thresholds).toList
  }
}

final case class WeightedScoring(len: Int, sps: List[ScoredProj]) extends Scoring {

  def overallScore(sl: List[ScoreTy], wl: List[ScoreTy]): ScoreTy = {
    sl.zip(wl).map { case (s, w) => s * w }.sum
  }

  override def sortedProjs: List[MatchedTy] = {
    val weights = Scoring.inverseWeightGen(len)
    sps.map { sp =>
      require(sp.scores.length == len)
      sp.projectMD -> overallScore(sp.scores, weights)
    }.sortBy(-_._2).map(_._1)
  }
}
