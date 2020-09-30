package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.{Literal, _}
import io.shiftleft.semanticcpg.language.{BaseNodeTypeDeco, ICallResolver, NoResolve}
import sg.edu.ntu.{Config, ProjectMD}
import sg.edu.ntu.TypeDefs.{MetricsTy, ScoreTy}
import sg.edu.ntu.matching.Similarity

import scala.collection.mutable
import scala.util.matching.Regex

object SemLiteral {
  val ignoredStrLiteralPattern: Regex = "%(d|u|lu|ul|s)".r
  val ignoredStrLiterals = List("ERROR")

  def literalFilter(l: Literal): Boolean = {
    l.typeFullName match {
      case "INT" => {
        val v = l.code.toInt
        v > 4
      }
      case "CHAR" => {
        true
      }
      case "FLOAT" => {
        true
      }
      case "DOUBLE" => {
        true
      }
      case "STRING" => {
        val s = l.code.toUpperCase
        s.length < Config.LiteralLen &&
          !ignoredStrLiterals.exists(s.contains(_)) &&
          ignoredStrLiteralPattern.findFirstIn(s).isEmpty
      }
      case _ => {
        logger.warn(s"unknown literal: ${l.code}:${l.typeFullName}")
        true
      }
    }
  }
}

case class SemLiteral(code: String, typeName: String)

case class SemIdentifier(name: String, typeFullName: String, label: String)

case class SpecialCalledArgsSM(sl: mutable.Set[Literal], si: mutable.Set[Identifier])

final case class MagicSem(projectMD: ProjectMD, cpg: Cpg, smms: List[SemMethod]) extends SMSem {

  implicit val myResolve: ICallResolver = NoResolve

  private val m2args = CpgWrapper.getAllFuncs(cpg).map(m => {
    m -> getCallSpecialArgs(m)
  }).toMap
  val literals: mutable.Set[Literal] = m2args.values.flatMap(_.sl).to(mutable.Set)
  val identifiers: mutable.Set[Identifier] = m2args.values.flatMap(_.si).to(mutable.Set)
  val semLiterals: Set[SemLiteral] = CpgWrapper.getSemLiterals(literals)
  val semIdentifiers: Set[SemIdentifier] = CpgWrapper.getSemIdentifiers(identifiers)

  val file2methods: Map[String, Set[Method]] = smms.map { m =>
    m.m.filename -> m.m
  }.groupBy(_._1).view.mapValues(_.map(_._2).toSet).toMap

  val metrics: Array[MetricsTy] = {
    val constM: MetricsTy = Math.min(literals.size + identifiers.size, Config.ConstMax)
    val SemConstM: MetricsTy = Math.min(semLiterals.size + semIdentifiers.size, Config.SemConstMax)
    val fileM: MetricsTy = Math.min(file2methods.size, Config.FileMax) / Config.FileF
    Array(constM, SemConstM, fileM)

  }

  val fileNum: MetricsTy = file2methods.size

  def getCallSpecialArgs(m: Method): SpecialCalledArgsSM = {
    val (sl, si) = m.start.call.map { call =>
      CpgWrapper.getSpecialArgs(call, SemLiteral.literalFilter)
    }.l.head
    SpecialCalledArgsSM(sl, si)
  }

  def getSpecialCallArgs: Map[Method, SpecialCalledArgsSM] = {
    CpgWrapper.getAllFuncs(cpg).map(m => {
      m -> getCallSpecialArgs(m)
    }).toMap
  }

  override def dumpAll(): Unit = {
    println(s"=== const info for ${projectMD} ===")
  }

  override def calculateSim(other: MagicSem.this.type): ScoreTy = {
    val s1 = Similarity.getJaccardSim(semIdentifiers, other.semIdentifiers)
    val s2 = Similarity.getCosineSim(metrics, other.metrics)
    (s1 + s2) / 2.0
  }
}