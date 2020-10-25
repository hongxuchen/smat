package sg.edu.ntu

import sg.edu.ntu.TypeDefs.MetricsTy

object Config {
  val BASEF: MetricsTy = 10
  val RecursiveF: MetricsTy = 20
  val SLocMax: Int = 150
  val SLocF: MetricsTy = 15
  val SLocSmallMax: Int = 8
  val CcMax: Int = 10
  val CfgEdgeMax: Int = 30
  val CfgEdgeF: MetricsTy = 3
  val CfgNodeMax: Int = CfgEdgeMax
  val CfgNodeF: MetricsTy = 3
  val ICalleeMax: Int = 10
  val ICallerMax: Int = ICalleeMax
  val ControlMax: Int = 30
  val ControlF: MetricsTy = 3
  val BranchMax: Int = 20
  val BranchF: MetricsTy = 2
  val LoopMax: Int = 20
  val LoopF: MetricsTy = 2
  val LoopSmallMax: Int = 3
  val DeepLoopMax: Int = 10
  val DeepLoopF: MetricsTy = 1
  val SpecialCallMax: Int = 20
  val SpecialCallF: MetricsTy = 2

  val LiteralLen: Int = 20
  val ConstMax: Int = 20
  val ConstF: Int = 2
  val SemConstMax: Int = 10
  val SemConstF: Int = 1
  val FileMax: Int = 50
  val FileF: Int = 5

  val InnerFeaturesFuncN: Int = 10
  val ScoreTops: Int = 10

  val PRECISION: Int = 1000000
}
