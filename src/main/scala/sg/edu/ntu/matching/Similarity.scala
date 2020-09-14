package sg.edu.ntu.matching

import smile.math.distance.JaccardDistance

import scala.jdk.CollectionConverters._

object Similarity {

  def getJaccardDist[A](sa: Set[A], sb: Set[A]): Double = {
    JaccardDistance.d(sa.asJava, sb.asJava)
  }

}
