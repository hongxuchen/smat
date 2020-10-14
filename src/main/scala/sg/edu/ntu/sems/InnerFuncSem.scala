package sg.edu.ntu.sems

import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.TypeDefs.{MetricsTy, ScoreTy}
import sg.edu.ntu.matching.Similarity
import sg.edu.ntu.{Config, ProjectMD}

final case class InnerFuncSem(projectMD: ProjectMD, smms: List[SemMethod]) extends SMSem {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def dumpAll(): Unit = {
    println(s"=== inner info for ${projectMD} ===")
    for ((m, i) <- smms.zipWithIndex) {
    }
  }

  val featureList: List[Array[MetricsTy]] = {
    val len = Math.min(smms.length, Config.InnerFeaturesFuncN)
    smms.take(len).map(smm => smm.asMethodFeatures)
  }

  /**
    * calculate similarities based on features, which is a list of N-Dimentional Array
    * each of the array demonstrates semantics of a specific method
    *
    * @param that
    * @return
    */
  override def calculateSim(that: InnerFuncSem.this.type): ScoreTy = {
    val len = Math.min(featureList.length, that.featureList.length)
    if (len < Config.ScoreTops) {
      logger.warn(s"Comparing ${len} features, this: ${featureList.length}, that:${that.featureList.length}")
    }
    val fl = featureList.zip(that.featureList).map { case (al, bl) => {
      Similarity.getCosineSim(al, bl)
    }
    }
    fl.sum / fl.length

  }
}
