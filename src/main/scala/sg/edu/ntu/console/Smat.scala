package sg.edu.ntu.console

import java.nio.file.{Files, Path}

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.fuzzyc2cpg.{FuzzyC2Cpg, SourceFiles}
import org.slf4j.LoggerFactory
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.console.PPConfig.runPP
import sg.edu.ntu.io.CpgLoader
import sg.edu.ntu.smsem.SExtract

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal


object Smat {

  val defaultExts = Set(".c", ".cc", ".cpp", ".h", ".hpp")

  private val logger = LoggerFactory.getLogger(this.getClass)

  case class ParserConfig(inputPaths: Set[String] = Set.empty,
                          semMatch: Boolean = false,
                          projectMD: ProjectMD = ProjectMD.DUMMY_PROJ,
                          srcExts: Set[String] = defaultExts,
                          forceUpdateCPG: Boolean = false,
                          forceUpdateSM: Boolean = false,
                          ppConfig: PPConfig = PPConfig()
                         )

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
      opt[Unit]("force-update-cpg")
        .text("force update cpg")
        .action((x, c) => c.copy(forceUpdateCPG = true))
      opt[Unit]("force-update-smdb")
        .text("force update SMDB")
        .action((x, c) => c.copy(forceUpdateSM = true))
      opt[String]('U', "undefine")
        .unbounded()
        .text("undefine a MACRO value")
        .action((u, cfg) =>
          cfg.copy(ppConfig = cfg.ppConfig.copy(defines = cfg.ppConfig.undefines + u)))
      opt[String]("pp-exe")
        .text("path to the preprocessor executable")
        .action((s, cfg) => cfg.copy(ppConfig = cfg.ppConfig.copy(ppExec = s)))
      opt[Unit]('M', "match")
        .text("flag to indicate a semantic match procedure")
        .action((_, c) => c.copy(semMatch = true))
      help("help").text("display this help message")
    }.parse(args, ParserConfig())

  def main(args: Array[String]): Unit = {
    val confOpt = parseConfig(args);
    confOpt.foreach { config =>
      try {
        val cpg = generateCpg(config)
        logger.info(s"analyze ${config.projectMD}")
        try {
          SExtract.analyze(config.projectMD, cpg)
          if (config.semMatch) {
            logger.info(s"proceed with semantic matching against ${config.projectMD}")
          }
        } finally {
          cpg.close()
        }
      } catch {
        case NonFatal(ex) => {
          logger.error("error when generating CPG/SMDB", ex)
        }
      }

    }
  }

}
