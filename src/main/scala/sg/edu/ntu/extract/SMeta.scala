package sg.edu.ntu.extract

case class SMeta(proj: String, version: String) {
  override def toString: String = proj + '-' + version
}