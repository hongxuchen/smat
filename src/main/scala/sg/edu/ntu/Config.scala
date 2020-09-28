package sg.edu.ntu

object Config {
  val BASEF: Int = 10
  val RecursiveF: Int = 20
  val SLocMax: Int = 150
  val SLocF: Int = 15
  val SLocSmallMax: Int = 8
  val CcMax: Int = 10
  val CfgEdgeMax: Int = 30
  val CfgEdgeF: Int = 3
  val CfgNodeMax: Int = CfgEdgeMax
  val CfgNodeF: Int = 3
  val ICalleeMax: Int = 10
  val ICallerMax: Int = ICalleeMax
  val ControlMax: Int = 30
  val ControlF: Int = 3
  val BranchMax: Int = 20
  val BranchF: Int = 2
  val LoopMax: Int = 20
  val LoopW: Int = 2
  val LoopSmallMax: Int = 3
  val DeepLoopMax: Int = 10
  val DeepLoopF: Int = 1
  val SpecialCallMax: Int = 20
  val SpecialCallF: Int = 2

  val InnerFeatures: Int = 10
  val ScoreTops: Int = 10
}
