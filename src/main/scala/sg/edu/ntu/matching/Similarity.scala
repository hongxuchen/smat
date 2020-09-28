package sg.edu.ntu.matching

import sg.edu.ntu.sems.MetricsTy
import smile.math.MathEx
import smile.math.distance.JaccardDistance

import scala.jdk.CollectionConverters._

object Similarity {

  type CosEleTy = Double

  def getJaccardSim[A](sa: Set[A], sb: Set[A]): Double = {
    JaccardDistance.d(sa.asJava, sb.asJava)
  }

  def getCosineSim(aa: Array[CosEleTy], ab: Array[CosEleTy]): Double = {
    MathEx.cos(aa, ab)
  }

  def getCosineSim(aa: Array[MetricsTy], ab: Array[MetricsTy]): Double = {
    getCosineSim(aa.map(_.toDouble), ab.map(_.toDouble))
  }

}
