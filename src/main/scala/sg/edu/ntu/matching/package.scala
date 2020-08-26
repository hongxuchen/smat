package sg.edu.ntu

package object matching {

  object Similarity {
    def weightGen(len: Int): List[Double] = {
      val d = Range(1, len + 1).reduce { (acc: Double, cur: Int) => acc + 1.0 / cur }
      List(1, len + 1).map { cur: Int => 1.0 / cur / d }
    }

    def thresholdGen(len: Int): List[Double] = {
      val d: Double = (len + 1) * len / 2
      List(1, len + 1).map { cur: Int => (cur: Double) / d }
    }

  }

  sealed trait Similarity {
  }

  final case class ThresholdSim(s: List[(Double, Double)]) extends Similarity {
  }

  final case class WeightedSim(s: List[(Double, Double)]) extends Similarity {
  }

}
