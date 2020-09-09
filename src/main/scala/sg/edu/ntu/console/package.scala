package sg.edu.ntu

import better.files.File
import org.slf4j.{Logger, LoggerFactory}


package object console {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val DEFAULT_CPG_OUT_FILE: String = "cpg" + RawSuffix
  val DEFAULT_CPG_IN_FILE: String = DEFAULT_CPG_OUT_FILE
  val DEFAULT_FUZZYPPCLI: String = "/usr/bin/fuzzyppcli"

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

}
