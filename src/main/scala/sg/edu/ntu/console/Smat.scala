package sg.edu.ntu.console

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.fuzzyc2cpg.FuzzyC2Cpg
import org.slf4j.LoggerFactory
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.console.PPConfig.runPP
import sg.edu.ntu.io.{CpgLoader, SmDBLoader}
import sg.edu.ntu.smsem.SMItem

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

    val dbDir = getDBDir
    val rawDB = dbDir / config.projectMD.asCpgFileName
    val rawDBFileName = rawDB.toString()

    if (rawDB.exists) {
      // if both cpg and smdb are not forced to update, do nothing
      if (!config.forceUpdateCPG && !config.forceUpdateSM) {
        logger.info(s"$rawDB exists, do nothing")
        val cpg = CpgLoader.loadFromOdb(rawDBFileName)
        return cpg
      } else {
        logger.info(s"${rawDB} force updating cpg")
      }
    }

    logger.info(s"output raw cpg to ${rawDBFileName}")

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
        val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(rawDBFileName))
        cpg
      }
    } else {
      logger.info("running w/o fuzzyppcli")
      val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(rawDBFileName))
      cpg
    }

  }

  /**
    * @param config
    * @return an instance of `SMItem` for future usage
    */
  def generateSMDB(config: ParserConfig, cpg: Cpg): SMItem = {
    val dbDir = getDBDir
    val smdbFile = dbDir / config.projectMD.asSmFileName
    val smdbFileName = smdbFile.toString()
    if (smdbFile.exists) {
      if (!config.forceUpdateSM) {
        logger.info(s"${smdbFile} exists, do nothing")
        val smItem = SmDBLoader.loadFromSMDB(smdbFileName)
        return smItem
      } else {
        logger.info(s"${smdbFile} force updating smdb")
      }
    }
    val smItem = SMItem(config.projectMD, cpg)
    smItem
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
      opt[Unit]("force-update-cpg")
        .text("force update cpg")
        .action((_, c) => c.copy(forceUpdateCPG = true))
      opt[Unit]("force-update-smdb")
        .text("force update SMDB")
        .action((_, c) => c.copy(forceUpdateSM = true))
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
      opt[Unit]('M', "match")
        .text("flag to indicate a semantic match procedure")
        .action((_, c) => c.copy(semMatch = true))
      help("help").text("display this help message")
    }.parse(args, ParserConfig())

  def main(args: Array[String]): Unit = {
    val confOpt = parseConfig(args)
    confOpt.foreach { config =>
      try {
        val cpg = generateCpg(config)
        logger.info(s"create SMDB for ${config.projectMD}")
        val smItem = generateSMDB(config, cpg)
        smItem.analyze()
        try {
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
