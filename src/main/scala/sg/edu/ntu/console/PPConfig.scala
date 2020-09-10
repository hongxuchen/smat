package sg.edu.ntu.console

import java.nio.file.{Files, Path}

import io.shiftleft.fuzzyc2cpg.SourceFiles
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

case class PPConfig(ppExec: String = PPConfig.DEFAULT_FUZZYPPCLI,
                    verbose: Boolean = true,
                    includeFiles: Set[String] = Set.empty,
                    includePaths: Set[String] = Set.empty,
                    defines: Set[String] = Set.empty,
                    undefines: Set[String] = Set.empty
                   ) {
  val usePP: Boolean =
    includeFiles.nonEmpty ||
      includePaths.nonEmpty ||
      defines.nonEmpty ||
      undefines.nonEmpty
}

object PPConfig {

  val DEFAULT_FUZZYPPCLI: String = "/usr/bin/fuzzyppcli"

  private val logger = LoggerFactory.getLogger(this.getClass)

  def runPP(sourcePaths: Set[String],
            srcExts: Set[String],
            includeFiles: Set[String],
            includePaths: Set[String],
            defs: Set[String],
            undefs: Set[String],
            ppExec: String): (Int, Path) = {
    // Create temp dir to store preprocessed source.
    val ppPath = Files.createTempDirectory("fuzzyc2cpg_pp_")
    logger.info(s"Writing preprocessed files to [$ppPath]")

    val ppLogFile = Files.createTempFile("fuzzyc2cpg_pp_log", ".txt").toFile
    logger.info(s"Writing preprocessor logs to [$ppLogFile]")

    val sourceFileNames = SourceFiles.determine(sourcePaths, srcExts)

    val commandBuffer = new ListBuffer[String]()
    commandBuffer.appendAll(List(ppExec, "--verbose", "-o", ppPath.toString))
    if (sourceFileNames.nonEmpty) commandBuffer.appendAll(List("-f", sourceFileNames.mkString(",")))
    if (includeFiles.nonEmpty) commandBuffer.appendAll(List("--include", includeFiles.mkString(",")))
    if (includePaths.nonEmpty) commandBuffer.appendAll(List("-I", includePaths.mkString(",")))
    if (defs.nonEmpty) commandBuffer.appendAll(List("-D", defs.mkString(",")))
    if (undefs.nonEmpty) commandBuffer.appendAll(List("-U", defs.mkString(",")))

    val cmd = commandBuffer.toList

    // Run preprocessor
    logger.info("Running preprocessor...")
    val process = new ProcessBuilder()
      .redirectOutput(ppLogFile)
      .redirectError(ppLogFile)
      .command(cmd: _*)
      .start()
    val exitCode = process.waitFor()

    if (exitCode == 0) {
      logger.info(s"Preprocessing complete, files written to [$ppPath], starting CPG generation...")
    } else {
      logger.error(
        s"Error occurred whilst running preprocessor. Log written to [$ppLogFile]. rc=[$exitCode]; analyzing w/o pp")
    }
    (exitCode, ppPath)
  }
}