package sg.edu.ntu.console

import io.shiftleft.fuzzyc2cpg.FuzzyC2Cpg
import org.slf4j.LoggerFactory
import sg.edu.ntu.ProjectMD

import scala.util.control.NonFatal


object FeatureSerializer extends App {

  val defaultExts = Set(".c", ".cc", ".cpp", ".h", ".hpp")

  private val logger = LoggerFactory.getLogger(this.getClass)

  case class ParserConfig(inputPaths: Set[String] = Set.empty,
                          projectMD: ProjectMD = ProjectMD.DUMMY_PROJ,
                          semanticsFile: String = CpgLoader.defaultSemanticsFile,
                          sourceFileExtensions: Set[String] = defaultExts,
                          preprocessorConfig: PreprocessorConfig = PreprocessorConfig())

  case class PreprocessorConfig(preprocessorExecutable: String = DEFAULT_FUZZYPPCLI_PATH,
                                verbose: Boolean = true,
                                includeFiles: Set[String] = Set.empty,
                                includePaths: Set[String] = Set.empty,
                                defines: Set[String] = Set.empty,
                                undefines: Set[String] = Set.empty) {
    val usePreprocessor: Boolean =
      includeFiles.nonEmpty || includePaths.nonEmpty || defines.nonEmpty || undefines.nonEmpty
  }

  def generateCpg(config: ParserConfig): Unit = {

    import better.files.Dsl._
    val dbDir = pwd / DB_DIR
    if (!dbDir.isDirectory) {
      logger.info(s"${dbDir} not existing, creating...")
      mkdir(dbDir)
    }
    val rawDB = dbDir / config.projectMD.asCpgFileName
    val rawDBFilePath = rawDB.toString()
    logger.info(s"output raw cpg to ${rawDB}")

    val fuzzyc = new FuzzyC2Cpg()
    if (config.preprocessorConfig.usePreprocessor) {
      logger.info("running w/ fuzzyppcli")
      fuzzyc.runWithPreprocessorAndOutput(
        config.inputPaths,
        config.sourceFileExtensions,
        config.preprocessorConfig.includeFiles,
        config.preprocessorConfig.includePaths,
        config.preprocessorConfig.defines,
        config.preprocessorConfig.undefines,
        config.preprocessorConfig.preprocessorExecutable
      )
    } else {
      logger.info("running w/o fuzzyppcli")
      fuzzyc.runAndOutput(config.inputPaths, config.sourceFileExtensions, Some(rawDBFilePath)).close()
    }

  }

  def parseConfig: Option[ParserConfig] =
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
      opt[String]("semfile")
        .text("data flow semantics file")
        .action((x, c) => c.copy(semanticsFile = x))
      opt[String]("src-ext")
        .unbounded()
        .text("source file extensions to include when gathering source files.")
        .action((pat, cfg) => cfg.copy(sourceFileExtensions = cfg.sourceFileExtensions + pat))
      opt[String]("include")
        .unbounded()
        .text("header include files")
        .action((incl, cfg) =>
          cfg.copy(preprocessorConfig =
            cfg.preprocessorConfig.copy(includeFiles = cfg.preprocessorConfig.includeFiles + incl)))
      opt[String]('I', "")
        .unbounded()
        .text("header include paths")
        .action((incl, cfg) =>
          cfg.copy(preprocessorConfig =
            cfg.preprocessorConfig.copy(includePaths = cfg.preprocessorConfig.includePaths + incl)))
      opt[String]('D', "define")
        .unbounded()
        .text("define a MACRO value")
        .action((d, cfg) =>
          cfg.copy(preprocessorConfig = cfg.preprocessorConfig.copy(defines = cfg.preprocessorConfig.defines + d)))
      opt[String]('U', "undefine")
        .unbounded()
        .text("undefine a MACRO value")
        .action((u, cfg) =>
          cfg.copy(preprocessorConfig = cfg.preprocessorConfig.copy(defines = cfg.preprocessorConfig.undefines + u)))
      opt[String]("pp-exe")
        .text("path to the preprocessor executable")
        .action((s, cfg) => cfg.copy(preprocessorConfig = cfg.preprocessorConfig.copy(preprocessorExecutable = s)))
      help("help").text("display this help message")
    }.parse(args, ParserConfig())

  parseConfig.foreach { config =>
    try {
      generateCpg(config)
    } catch {
      case NonFatal(ex) => {
        logger.error("failed to enhance CPG", ex)
      }
    }

  }

}
