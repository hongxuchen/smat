package sg.edu.ntu.console

import io.shiftleft.fuzzyc2cpg.FuzzyC2Cpg
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal


object FeatureExtract extends App {

  private val logger = LoggerFactory.getLogger(this.getClass)

  case class ParserConfig(inputPaths: Set[String] = Set.empty,
                          outputCpgFile: String = DEFAULT_CPG_OUT_FILE,
                          enhance: Boolean = true,
                          dataFlow: Boolean = true,
                          enhanceOnly: Boolean = false,
                          semanticsFile: String = CpgLoader.defaultSemanticsFile,
                          sourceFileExtensions: Set[String] = Set(".c", ".cc", ".cpp", ".h", ".hpp"),
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

    if (config.enhanceOnly == false) {
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
        fuzzyc.runAndOutput(config.inputPaths, config.sourceFileExtensions, Some(config.outputCpgFile)).close()
      }
    }

    if (config.enhance) {
      CpgEnhancer.run(config.outputCpgFile, config.dataFlow, config.semanticsFile).close()
    }

  }

  def parseConfig: Option[ParserConfig] =
    new scopt.OptionParser[ParserConfig](getClass.getSimpleName) {
      help("help")
      arg[String]("<input-dir>")
        .unbounded()
        .text("source directories containing C/C++ code")
        .action((x, c) => c.copy(inputPaths = c.inputPaths + x))
      opt[String]("out")
        .text("output filename")
        .action((x, c) => c.copy(outputCpgFile = x))
      opt[Unit]("noenhance")
        .text("run language frontend but do not enhance the CPG to create an SCPG")
        .action((x, c) => c.copy(enhance = false))
      opt[Unit]("enhanceonly")
        .text("Only run the enhancer")
        .action((x, c) => c.copy(enhanceOnly = true))
      opt[Unit]("nodataflow")
        .text("do not perform data flow analysis")
        .action((x, c) => c.copy(dataFlow = false))
      opt[String]("semanticsfile")
        .text("data flow semantics file")
        .action((x, c) => c.copy(semanticsFile = x))
      opt[String]("source-file-ext")
        .unbounded()
        .text("source file extensions to include when gathering source files. Defaults are .c, .cc, .cpp, .h and .hpp")
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
        .text("define a name")
        .action((d, cfg) =>
          cfg.copy(preprocessorConfig = cfg.preprocessorConfig.copy(defines = cfg.preprocessorConfig.defines + d)))
      opt[String]('U', "undefine")
        .unbounded()
        .text("undefine a name")
        .action((u, cfg) =>
          cfg.copy(preprocessorConfig = cfg.preprocessorConfig.copy(defines = cfg.preprocessorConfig.undefines + u)))
      opt[String]("preprocessor-executable")
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
