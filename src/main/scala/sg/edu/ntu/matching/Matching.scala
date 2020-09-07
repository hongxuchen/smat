package sg.edu.ntu.matching

abstract class Strategy(simList: List[Similarity]) {
  def run(): Unit
}

final class WeightedStrategy(simList: List[Similarity]) extends Strategy(simList) {
  override def run(): Unit = ???
}

final class LayeredStrategy(simList: List[Similarity]) extends Strategy(simList) {
  override def run(): Unit = ???
}

object Matching {

}
