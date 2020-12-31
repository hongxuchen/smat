package sg.edu.ntu

case class ModuleMD(module: String, version: String) {
  // to printable string
  override def toString: String = ModuleMD.normalizedString(module) + '-' + ModuleMD.normalizedString(version)

  def asCpgFileName: String = toString + RawSuffix

  def asSmFileName: String = toString + SmSuffix
}

object ModuleMD {

  val DUMMY_MODULE: ModuleMD = ModuleMD("__DUMMY__", "__DUMMY__")

  def apply(s: String): ModuleMD = {
    val ss = s.split('-')
    require(ss.length == 2)
    ModuleMD(ss(0), ss(1))
  }

  def normalizedString(s: String): String = {
    s.replaceAll("\\s+", "_").replaceAll("\\p{C}", ".")
  }

  def getRawDB(md: ModuleMD): String = {
    md.toString + RawSuffix
  }

  def getSMDB(md: ModuleMD): String = {
    md.toString + SmSuffix
  }
}