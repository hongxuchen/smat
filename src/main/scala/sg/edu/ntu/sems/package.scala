package sg.edu.ntu

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.matching.ScoreTy

package object sems {

  type MetricsTy = Integer

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def optStr[A](optV: Option[A], defaultStr: String = "NA"): String = {
    optV match {
      case Some(v) => v.toString
      case None => defaultStr
    }
  }

  def dumpToFile(s: String, dirName: String, label: String, fileType: String): Unit = {
    val sysTempDir = System.getProperty("java.io.tmpdir")
    val tmpDir = Paths.get(sysTempDir, dirName)
    if (!Files.isDirectory(tmpDir)) {
      Files.createDirectory(tmpDir)
    }
    val fileName = label + '.' + fileType
    val filePath = Paths.get(tmpDir + File.separator + fileName)
    try {
      Files.write(filePath, s.getBytes())
    } catch {
      case e: IOException => {
        e.printStackTrace()
      }
      case e: Throwable => {
        logger.error(s"unknown exception: ${e.getClass}, ${e.getMessage}")
      }
    }
  }

  /**
    * abstract class to provide semantics for semantic matching
    */
  abstract class SMSem() {

    def dumpAll(): Unit

    def calculateSim(other: this.type): ScoreTy

  }

}
