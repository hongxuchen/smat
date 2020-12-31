package sg.edu.ntu.matching

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.ModuleMD

class ScoringSpec extends AnyFlatSpec with Matchers {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def ~=(x: Double, y: Double, precision: Double = 0.001): Boolean = (x - y).abs < precision

  val sps: List[ScoredMod] = {
    List(ScoredMod(ModuleMD("hw-1.0"), List(0.7, 0.4, 0.1)),
      ScoredMod(ModuleMD("hw-1.1"), List(0.55, 0.35, 0.9)),
      ScoredMod(ModuleMD("hw-1.2"), List(0.5, 0.45, 0.6)))
  }

  val m0 = List(ModuleMD("hw-1.0"), ModuleMD("hw-1.1"), ModuleMD("hw-1.2"))
  val m1 = List(ModuleMD("hw-1.0"), ModuleMD("hw-1.2"), ModuleMD("hw-1.1"))
  val m2 = List(ModuleMD("hw-1.1"), ModuleMD("hw-1.2"), ModuleMD("hw-1.0"))

  val LEN = 3

  "The Hello object" should "say hello" in {
    val weights = Scoring.inverseWeightGen(LEN)
    logger.debug(s"weights: ${weights}")
    val thresholds = Scoring.thresholdGen(LEN)
    logger.debug(s"thresholds: ${thresholds}")
  }

  "weighted scoring" should "weight" in {
    WeightedScoring(sps).sortedMods shouldBe m2
  }

  "threshold scoring" should "threshold" in {
    val gridGen = Scoring.gridThresholdGen(LEN)
    val expected = List(0.6, 0.4, 0.2)
    for ((g, e) <- gridGen.zip(expected)) {
      ~=(g, e) shouldBe true
    }
    ThresholdScoring(sps).getTops(5) shouldBe m1
  }

}
