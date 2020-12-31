package sg.edu.ntu.sems


import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import sg.edu.ntu.ModuleMD
import sg.edu.ntu.matching.ScoredMod

import scala.collection.mutable.ListBuffer


@SerialVersionUID(100L)
case class SMItem(moduleMD: ModuleMD, sems: ListBuffer[SMSem]) extends Serializable {

  def appendSem(sem: SMSem): Unit = {
    logger.info(s"adding ${sem.getClass} to ${moduleMD}")
    sems.append(sem)
  }

  def dump(): Unit = {
    for (sem <- sems) {
      sem.dumpAll()
    }
  }


  def calculateSim(other: SMItem): ScoredMod = {
    val scores = this.sems.zip(other.sems).map { case (sm1, sm2) => {
      sm1.calculateSim(sm2.asInstanceOf[sm1.type])
    }
    }.toList
    ScoredMod(other.moduleMD, scores)
  }
}

object SMItem {

  import CpgWrapper._

  def getSemMethods(cpg: Cpg): List[SemMethod] = {
    val methods: List[Method] = getInterestingFuncs(cpg)
    methods.map { m => SemMethod(m) }.sorted
  }

  /**
    * instance factory for `SMItem`, with all the semantic fields
    *
    * @param moduleMD
    * @param cpg
    * @return
    */
  def apply(moduleMD: ModuleMD, cpg: Cpg): SMItem = {
    val semMethods = getSemMethods(cpg)
    val smItem = new SMItem(moduleMD, ListBuffer.empty)
    val magicSem = MagicSem(moduleMD, cpg, semMethods)
    smItem.appendSem(magicSem)
    val interSem = InterFuncSem(moduleMD, getAllFuncs(cpg), semMethods)
    smItem.appendSem(interSem)
    val innerSem = InnerFuncSem(moduleMD, semMethods)
    smItem.appendSem(innerSem)
    smItem
  }

  def writeCfgToDot(moduleMD: ModuleMD, cpg: Cpg): Unit = {
    import better.files.Dsl._
    import io.shiftleft.semanticcpg.language._
    val dirName = "dots_" + moduleMD
    val dir = sg.edu.ntu.serde.Utils.getDBDir / dirName
    if (!dir.isDirectory) {
      mkdir(dir)
    }

    cpg.method.internal.foreach{ m =>
      logger.info(m.name + "\n" + m.start.dumpRaw.mkString)
    }

    val s = cpg.method("ObcCmdStatusMerge").dumpRaw.mkString
    logger.info(s)

    //    val semMethods = getSemMethods(cpg)
//    for (m <- semMethods) {
//      val method = m.m
//      val s = method.start.dotCfg.l.head
//      logger.info(method.start.dumpRaw.mkString)
//      assert(s != null)
//      val dotFilePath = dir / (m.m.name + ".dot")
//      logger.info(s"${m.m.name} -> ${dotFilePath}")
//      dotFilePath.write(s)
//    }
  }

}
