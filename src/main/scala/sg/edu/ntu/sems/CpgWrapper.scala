package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes
import io.shiftleft.codepropertygraph.generated.nodes._
import io.shiftleft.dataflowengineoss.language.toTrackingPoint
import io.shiftleft.semanticcpg.dotgenerator.Shared.Edge
import io.shiftleft.semanticcpg.language._
import overflowdb.Node
import sg.edu.ntu.Config

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object CpgWrapper {

  def getInterestingFuncs(cpg: Cpg): List[Method] = {
    cpg.method.internal.where(m => !MethodWrapper.isIgnoredMethod(m)).l
  }

  def getAllFuncs(cpg: Cpg): List[Method] = {
    cpg.method.internal.l
  }

  def getConstFuncs(cpg: Cpg): List[Method] = {
    cpg.method.internal.where { m =>
      !m.signature.contains("const") && MethodWrapper.callOutsAreConst(m) && MethodWrapper.parameterOpsAreConst(m)
    }.toList()

  }

  def getSpecialArgs(call: Call, filter: Literal => Boolean = _ => true): (mutable.Set[Literal], mutable.Set[Identifier]) = {

    val literals: mutable.Set[Literal] = mutable.Set.empty
    val identifiers: mutable.Set[Identifier] = mutable.Set.empty

    call.start.argument.map {
      case id: Identifier => {
        if (id.name.toUpperCase == id.name) {
          identifiers.add(id)
        }
      }
      case literal: Literal => {
        if (filter(literal)) {
          literals.add(literal)
        }
      }
      case exp => {
        logger.warn(s"unhandled: ${exp}:${exp.getClass}")
      }
    }
    (literals, identifiers)
  }

  def getSemLiterals(literals: mutable.Set[Literal]): Set[SemLiteral] = {
    literals.map(l => SemLiteral(l.code, l.typeFullName)).toSet
  }

  def getSemIdentifiers(identifiers: mutable.Set[Identifier]): Set[SemIdentifier] = {
    identifiers.map(l => SemIdentifier(l.name, l.typeFullName, l.label)).toSet
  }

}

object MethodWrapper {

  implicit val curResolve: ICallResolver = NoResolve

  def callOutsAreConst(m: Method): Boolean = {
    m.start.callee.internal.l.forall(_.signature.contains("const"))
  }

  def parameterOpsAreConst(m: Method): Boolean = {
    m.start.parameter.inAssignment.source.l.forall {
      case c: Call => c.signature.contains("const")
      case _ => false
    } && m.start.assignments.target.reachableBy(m.start.parameter).isEmpty
  }

  def sloc(m: Method): Option[Int] = {
    (m.lineNumber, m.lineNumberEnd) match {
      case (Some(start), Some(end)) => {
        Some(end - start + 1)
      }
      case _ => None
    }
  }

  def isInterestingCfgNode(v: Node): Boolean = !(
    v.isInstanceOf[nodes.Literal] ||
      v.isInstanceOf[nodes.Identifier] ||
      v.isInstanceOf[nodes.Block] ||
      v.isInstanceOf[nodes.ControlStructure] ||
      v.isInstanceOf[nodes.JumpTarget] ||
      v.isInstanceOf[nodes.MethodParameterIn]
    )

  /**
    * taken from `DotCfgGenerator`
    *
    * @param v
    * @return
    */
  def expandedStoredNodes(v: nodes.StoredNode): Iterator[Edge] = {
    v._cfgOut()
      .asScala
      .filter(_.isInstanceOf[nodes.StoredNode])
      .map(node => Edge(v, node))
  }

  private def getCfgEdges(methodNode: nodes.Method,
                          expand: nodes.StoredNode => Iterator[Edge],
                          cfgNodeShouldBeDisplayed: Node => Boolean): List[Edge] = {
    val vertices = methodNode.start.cfgNode.l ++ List(methodNode, methodNode.methodReturn) ++ methodNode.parameter.l
    val verticesToDisplay = vertices.filter(cfgNodeShouldBeDisplayed)

    def _cfgEdges(srcNode: nodes.StoredNode, visited: List[nodes.StoredNode] = List()): List[Edge] = {
      if (visited.contains(srcNode)) {
        List()
      } else {
        val children = expand(srcNode).filter(x => vertices.contains(x.dst))
        val (visible, invisible) = children.partition(x => cfgNodeShouldBeDisplayed(x.dst))
        visible.toList ++ invisible.toList.flatMap { n =>
          _cfgEdges(n.dst, visited ++ List(srcNode)).map(y => Edge(srcNode, y.dst))
        }
      }
    }

    val edges = verticesToDisplay.map { v =>
      _cfgEdges(v)
    }

    edges.flatten
  }

  def getName(m: Method): String = m.fullName

  def getMethodCfgNodes(m: Method): List[nodes.CfgNode] = {
    m.start.cfgNode.where { n =>
      isInterestingCfgNode(n)
    }.l
  }

  def getMethodCfgEdges(m: Method): List[Edge] = {
    m.start.map { mm =>
      getCfgEdges(mm, expandedStoredNodes, isInterestingCfgNode)
    }.l.head
  }

  def inSameSrcFile(m1: Method, m2: Method): Boolean = m1.filename == m2.filename

  def specialCallees(method: Method, funcs: Set[String]): Set[String] = {
    method.start.callee.name.where(n => funcs.contains(n)).toSet
  }

  /**
    * @param m
    * @param filter
    * @return immediate calless defined in this component
    */
  def getICallees(m: Method, filter: Method => Boolean): Set[Method] = {
    m.start.callee.internal.where(m => filter(m)).toSet()
  }

  def getICallers(m: Method, filter: Method => Boolean): Set[Method] = {
    m.start.caller.where(filter).toSet()
  }

  def cfgStr(m: Method): String = m.start.dotCfg.l().head

  def astStr(m: Method): String = m.start.dotAst.l().head

  def getControlStructs(m: Method): List[ControlStructure] = {
    m.start.ast.isControlStructure.l
  }

  def getBranchStructs(m: Method): List[ControlStructure] = {
    m.start.ast.isControlStructure.parserTypeName("(If|Else).*").l
  }

  def getLoopStructs(m: Method): List[ControlStructure] = {
    m.start.ast.isControlStructure.parserTypeName("(For|Do|While).*").l
  }

  def getLoopDepths(m: Method): Int = {
    m.start.map { mm =>
      mm.depth(ast => {
        ast match {
          case c: ControlStructure => {
            val tName = c.parserTypeName
            tName.startsWith("For") || tName.startsWith("While") || tName.startsWith("Do")
          }
          case _ => false
        }

      })
    }.l.head
  }

  def isSmall(m: Method): Boolean = {
    MethodWrapper.sloc(m) match {
      case Some(line) => line < Config.SLocSmallMax
      case _ => false
    }
  }

  def isSelfRecursive(m: Method): Boolean = {
    val calledMethodOpt = m.start.call(m.name).callee.l.headOption
    calledMethodOpt match {
      case Some(calledMethod) => calledMethod == m
      case _ => false
    }
  }

  def isInline(m: Method): Boolean = {
    m.signature.contains("inline")
  }

  def isInternal(m: Method): Boolean = {
    m.name.startsWith("_")
  }

  def isIgnoredMethod(m: Method): Boolean = {
    !isSelfRecursive(m) && isInline(m) && isInternal(m) || isSmall(m)
  }

  def getBlocks(m: Method): List[Block] = {
    m.start.block.l
  }

}