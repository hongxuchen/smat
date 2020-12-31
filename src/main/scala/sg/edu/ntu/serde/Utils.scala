package sg.edu.ntu.serde

import better.files.File
import io.shiftleft.codepropertygraph.Cpg
import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.{ModuleMD, RawSuffix}

object Utils {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val DEFAULT_CPG_OUT_FILE: String = "cpg" + RawSuffix
  val DEFAULT_CPG_IN_FILE: String = DEFAULT_CPG_OUT_FILE

  val DB_DIR = "SMDB"

  val getDBDir: File = {
    import better.files.Dsl._
    val dbDir = pwd / DB_DIR
    if (!dbDir.isDirectory) {
      logger.info("{} not exists, creating...", dbDir)
      mkdir(dbDir)
    }
    dbDir
  }

  def getCpgDBPath(ModuleMd: ModuleMD): better.files.File = {
    getDBDir / ModuleMd.asCpgFileName
  }

  def getSMDBPath(moduleMD: ModuleMD): better.files.File = {
    getDBDir / moduleMD.asSmFileName
  }

  def getSMDBFpaths: List[String] = {
    getDBDir.list(_.`extension`.contains(".sm")).map(_.toString).toList.sorted
  }

  def getCpgFromModuleID(moduleMD: ModuleMD): Option[Cpg] = {
    val rawDB = getCpgDBPath(moduleMD)
    if (rawDB.isRegularFile) {
      val rawDBPathStr = rawDB.toString()
      val cpg = CpgLoader.loadFromOdb(rawDBPathStr)
      Some(cpg)
    } else {
      None
    }
  }

}
