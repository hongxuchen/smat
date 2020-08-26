package sg.edu.ntu

case class ProjectMD(proj: String, version: String) {
  // to printable string
  override def toString: String = ProjectMD.normalizedString(proj) + '-' + ProjectMD.normalizedString(version)

  def asCpgFileName: String = toString + RawSuffix

  def asSmFileName: String = toString + SmSuffix
}

object ProjectMD {

  val DUMMY_PROJ = ProjectMD("__DUMMY__", "__DUMMY__")

  def apply(s: String): ProjectMD = {
    val ss = s.split('-')
    require(ss.length == 2)
    ProjectMD(ss(0), ss(1))
  }

  def normalizedString(s: String): String = {
    s.replaceAll("\\s+", "_").replaceAll("\\p{C}", ".")
  }

  def getRawDB(md: ProjectMD): String = {
    md.toString + RawSuffix
  }

  def getSMDB(md: ProjectMD): String = {
    md.toString + SmSuffix
  }
}