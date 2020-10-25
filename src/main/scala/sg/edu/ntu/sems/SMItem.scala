package sg.edu.ntu.sems


import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.matching.ScoredProj

import scala.collection.mutable.ListBuffer



@SerialVersionUID(100L)
case class SMItem(projectMD: ProjectMD, sems: ListBuffer[SMSem]) extends Serializable {

  def appendSem(sem: SMSem): Unit = {
    logger.info(s"adding ${sem.getClass} to ${projectMD}")
    sems.append(sem)
  }

  def dump(): Unit = {
    for (sem <- sems) {
      sem.dumpAll()
    }
  }

  def calculateSim(other: SMItem): ScoredProj = {
    val scores = this.sems.zip(other.sems).map { case (sm1, sm2) => {
      sm1.calculateSim(sm2.asInstanceOf[sm1.type])
    }
    }.toList
    ScoredProj(other.projectMD, scores)
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
    * @param projectMD
    * @param cpg
    * @return
    */
  def apply(projectMD: ProjectMD, cpg: Cpg): SMItem = {
    val semMethods = getSemMethods(cpg)
    val smItem = new SMItem(projectMD, ListBuffer.empty)
    val magicSem = MagicSem(projectMD, cpg, semMethods)
    smItem.appendSem(magicSem)
    val interSem = InterFuncSem(projectMD, getAllFuncs(cpg), semMethods)
    smItem.appendSem(interSem)
    val innerSem = InnerFuncSem(projectMD, semMethods)
    smItem.appendSem(innerSem)
    smItem
  }
}
