package sg.edu.ntu.console

import java.nio.file.{Files, Path}

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.fuzzyc2cpg.{FuzzyC2Cpg, SourceFiles}
import org.slf4j.LoggerFactory
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.smsem.CpgLoader

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal


object FeatureSerialize {

  val defaultExts = Set(".c", ".cc", ".cpp", ".h", ".hpp")

  private val logger = LoggerFactory.getLogger(this.getClass)

  case class ParserConfig(inputPaths: Set[String] = Set.empty,
                          projectMD: ProjectMD = ProjectMD.DUMMY_PROJ,
                          srcExts: Set[String] = defaultExts,
                          forceUpdateCPG: Boolean = false,
                          forceUpdateSM: Boolean = false,
                          ppConfig: PPConfig = PPConfig()
                         )

  case class PPConfig(ppExec: String = DEFAULT_FUZZYPPCLI,
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

  /**
   *
   * @param config parse config
   * @return an active `Cpg` to be used (need closing before exiting)
   */
  def generateCpg(config: ParserConfig): Cpg = {

    import better.files.Dsl._
    val dbDir = pwd / DB_DIR
    if (!dbDir.isDirectory) {
      logger.info(s"${dbDir} not exists, creating...")
      mkdir(dbDir)
    }
    val rawDB = dbDir / config.projectMD.asCpgFileName
    val rawDBFileName = rawDB.toString()

    if (rawDB.exists) {
      if (!config.forceUpdateCPG) {
        logger.info(s"${rawDB} exists, do nothing")
        val cpg = CpgLoader.loadFromOdb(rawDBFileName)
        return cpg
      } else {
        logger.info(s"${rawDB} force updating")
      }
    }

    val rawDBFilePath = rawDB.toString()
    logger.info(s"output raw cpg to ${rawDBFilePath}")

    val fuzzyc = new FuzzyC2Cpg()
    if (config.ppConfig.usePP) {
      logger.info("running w/ fuzzyppcli")
      val (exitCode, ppPath) = runPP(
        config.inputPaths,
        config.srcExts,
        config.ppConfig.includeFiles,
        config.ppConfig.includePaths,
        config.ppConfig.defines,
        config.ppConfig.undefines,
        config.ppConfig.ppExec,
      )
      if (exitCode == 0) {
        val cpg = fuzzyc.runAndOutput(Set(ppPath.toString), config.srcExts, None)
        cpg
      } else {
        val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(rawDBFilePath))
        cpg
      }
    } else {
      logger.info("running w/o fuzzyppcli")
      val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(rawDBFilePath))
      cpg
    }

  }

  def parseConfig(args: Array[String]): Option[ParserConfig] =
    new scopt.OptionParser[ParserConfig](getClass.getSimpleName) {
      help("help")
      arg[String]("<src-dir>")
        .unbounded()
        .text("source directories containing C/C++ code")
        .action((x, c) => c.copy(inputPaths = c.inputPaths + x))
      opt[String]("proj")
        .text("project metadata to be specified")
        .required()
        .action((x, c) => c.copy(projectMD = ProjectMD(x)))
      opt[String]("src-ext")
        .unbounded()
        .text("source file extensions to include when gathering source files.")
        .action((pat, cfg) => cfg.copy(srcExts = cfg.srcExts + pat))
      opt[String]("include")
        .unbounded()
        .text("header include files")
        .action((incl, cfg) =>
          cfg.copy(ppConfig =
            cfg.ppConfig.copy(includeFiles = cfg.ppConfig.includeFiles + incl)))
      opt[String]('I', "")
        .unbounded()
        .text("header include paths")
        .action((incl, cfg) =>
          cfg.copy(ppConfig =
            cfg.ppConfig.copy(includePaths = cfg.ppConfig.includePaths + incl)))
      opt[String]('D', "define")
        .unbounded()
        .text("define a MACRO value")
        .action((d, cfg) =>
          cfg.copy(ppConfig = cfg.ppConfig.copy(defines = cfg.ppConfig.defines + d)))
      opt[String]('U', "undefine")
        .unbounded()
        .text("undefine a MACRO value")
        .action((u, cfg) =>
          cfg.copy(ppConfig = cfg.ppConfig.copy(defines = cfg.ppConfig.undefines + u)))
      opt[String]("pp-exe")
        .text("path to the preprocessor executable")
        .action((s, cfg) => cfg.copy(ppConfig = cfg.ppConfig.copy(ppExec = s)))
      help("help").text("display this help message")
    }.parse(args, ParserConfig())

  def main(args: Array[String]): Unit = {
    val confOpt = parseConfig(args);
    confOpt.foreach { config =>
      try {
        val cpg = generateCpg(config)
      } catch {
        case NonFatal(ex) => {
          logger.error("error when generating CPG/SMDB", ex)
        }
      }

    }
  }

}
