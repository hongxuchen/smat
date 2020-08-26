package sg.edu.ntu.matching

abstract class Strategy(simList: List[Similarity]) {
  
}

final class WeightedStrategy(simList: List[Similarity]) extends Strategy(simList)

final class LayeredStrategy(simList: List[Similarity]) extends Strategy(simList)

object Matching extends App {

}
