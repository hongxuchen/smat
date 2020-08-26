package sg.edu.ntu

package object matching {

  type ProjSimMap = Map[ProjectMD, Similarity]

  trait SMatcherT

  object Similarity {
    def weightGen(len: Int): List[Double] = {
       val d = Range(1, len + 1).foldLeft(0.0d)((acc, cur) => acc + 1.0 / cur.toDouble)
       List(1, len + 1).map { cur: Int => 1.0 / cur.toDouble / d }
    }

    def thresholdGen(len: Int): List[Double] = {
       val d: Double = (len + 1) * len / 2
       List(1, len + 1).map { cur: Int => cur.toDouble / d }
    }

  }

  sealed trait Similarity {
  }

  final case class ThresholdSim(s: List[(Double, Double)]) extends Similarity {

  }

  final case class WeightedSim(s: List[(Double, Double)]) extends Similarity {
  }

}
