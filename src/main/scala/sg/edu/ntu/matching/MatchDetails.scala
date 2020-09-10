package sg.edu.ntu.matching

import io.shiftleft.codepropertygraph.Cpg
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.serde.Utils
import java.io.IOException

object DumpStyle extends Enumeration {
  type DumpStyle = Value
  val Simple, Verbose = Value
}

/**
  * class to display specially matched structures (function level) in the projects
  *
  * @param cpg1 original project
  * @param cpg2 matching project
  */
case class MatchDetails(cpg1: Cpg, cpg2: Cpg) {
  def dump(style: DumpStyle.DumpStyle): Unit = {
    style match {
      case DumpStyle.Simple => {
        ???
      }
      case DumpStyle.Verbose => {
        ???
      }
    }
  }
}

object MatchDetails {
  def apply(proj1: ProjectMD, proj2: ProjectMD): MatchDetails = {
    val cpgOpt1 = Utils.getCpgFromProjID(proj1)
    val cpgOpt2 = Utils.getCpgFromProjID(proj2)
    (cpgOpt1, cpgOpt2) match {
      case (Some(cpg1), Some(cpg2)) => {
        MatchDetails(cpg1, cpg2)
      }
      case _ => {
        throw new IOException("cpg info incomplete")
      }
    }
  }
}