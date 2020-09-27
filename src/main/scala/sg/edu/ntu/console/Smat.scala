package sg.edu.ntu.console

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.fuzzyc2cpg.FuzzyC2Cpg
import org.slf4j.LoggerFactory
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.matching.{NDimScoring, ScoredProj, Scoring, ThresholdScoring, WeightedScoring}
import sg.edu.ntu.serde.{CpgLoader, SmDBSerde, Utils}
import sg.edu.ntu.sems.SMItem

import scala.util.control.NonFatal

object ScoreEnum extends Enumeration {
  type ScoreEnum = Value
  val NDim, Threshold, Weighted = Value

  def getScoring(v: ScoreEnum, sps: List[ScoredProj]): Scoring = {
    v match {
      case NDim => NDimScoring(sps)
      case Threshold => ThresholdScoring(sps)
      case Weighted => WeightedScoring(sps)
    }
  }
}

object Smat {

  implicit val scoreRead: scopt.Read[ScoreEnum.ScoreEnum] = scopt.Read.reads(ScoreEnum withName _)

  val defaultExts = Set(".c", ".cc", ".cpp", ".h", ".hpp")

  private val logger = LoggerFactory.getLogger(this.getClass)

  case class ParserConfig(inputPaths: Set[String] = Set.empty,
                          semMatch: Boolean = false,
                          smScoring: ScoreEnum.ScoreEnum = ScoreEnum.NDim,
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

    val cpgDB = Utils.getCpgDBPath(config.projectMD)
    val cpgDBPathStr = cpgDB.toString()

    if (cpgDB.exists) {
      // if both cpg and smdb are not forced to update, do nothing
      if (!config.forceUpdateCPG && !config.forceUpdateSM) {
        logger.info(s"$cpgDB exists, do nothing")
        val cpg = CpgLoader.loadFromOdb(cpgDBPathStr)
        return cpg
      } else {
        logger.info(s"${cpgDB} force updating cpg")
      }
    }

    logger.info(s"output raw cpg to ${cpgDBPathStr}")

    val fuzzyc = new FuzzyC2Cpg()
    if (config.ppConfig.usePP) {
      logger.info("running w/ fuzzyppcli")
      val (exitCode, ppPath) = PPConfig.runPP(
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
        val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(cpgDBPathStr))
        cpg
      }
    } else {
      logger.info("running w/o fuzzyppcli")
      val cpg = fuzzyc.runAndOutput(config.inputPaths, config.srcExts, Some(cpgDBPathStr))
      cpg
    }

  }

  /**
    * @param config
    * @return an instance of `SMItem` for future usage
    */
  def generateSMDB(config: ParserConfig, cpg: Cpg): SMItem = {
    val smdbPath = Utils.getSMDBPath(config.projectMD)
    val smdbFileName = smdbPath.toString()
    if (smdbPath.exists) {
      if (!config.forceUpdateSM) {
        logger.info(s"${smdbFileName} exists, do nothing")
        val smItem = SmDBSerde.load(smdbFileName)
        return smItem
      } else {
        logger.info(s"${smdbFileName} force updating smdb")
      }
    }
    logger.info("generating smdb...")
    val smItem = SMItem(config.projectMD, cpg)
    SmDBSerde.write(smItem)
    smItem
  }

  def dumpMatched(targetSmItem: SMItem): Unit = {
    val scoredProjs: List[ScoredProj] = Utils.getSMDBFpaths.map { fpath =>
      val otherSmItem = SmDBSerde.load(fpath)
      targetSmItem.calculateSim(otherSmItem)
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
      opt[ScoreEnum.ScoreEnum]("score")
        .text("matching scoring strategy")
        .action((s, c) => c.copy(smScoring = s))
      help("help").text("display this help message")
    }.parse(args, ParserConfig())

  def main(args: Array[String]): Unit = {
    val confOpt = parseConfig(args)
    confOpt.foreach { config =>
      try {
        val cpg = generateCpg(config)
        logger.info(s"create SMDB for ${config.projectMD}")
        val smItem: SMItem = generateSMDB(config, cpg)
        smItem.dump()
        try {
          if (config.semMatch) {
            logger.info(s"proceed with semantic matching against ${config.projectMD}")
            dumpMatched(smItem)
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
