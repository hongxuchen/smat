package sg.edu.ntu.serde

import better.files.File
import io.shiftleft.codepropertygraph.Cpg
import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.{ProjectMD, RawSuffix}

object Utils {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val DEFAULT_CPG_OUT_FILE: String = "cpg" + RawSuffix
  val DEFAULT_CPG_IN_FILE: String = DEFAULT_CPG_OUT_FILE

  val DB_DIR = "SMDB"

  val getDBDir: File = {
    import better.files.Dsl._
    val dbDir = pwd / DB_DIR
    if (!dbDir.isDirectory) {
      logger.info(s"${dbDir} not exists, creating...")
      mkdir(dbDir)
    }
    dbDir
  }

  def getCpgDBPath(projectMD: ProjectMD): better.files.File = {
    getDBDir / projectMD.asCpgFileName
  }

  def getSMDBPath(projectMD: ProjectMD): better.files.File = {
    getDBDir / projectMD.asSmFileName
  }

  def getCpgFromProjID(projectMD: ProjectMD): Option[Cpg] = {
    val rawDB = getCpgDBPath(projectMD)
    if (rawDB.isRegularFile) {
      val rawDBPathStr = rawDB.toString()
      val cpg = CpgLoader.loadFromOdb(rawDBPathStr)
      Some(cpg)
    } else {
      None
    }
  }

}
