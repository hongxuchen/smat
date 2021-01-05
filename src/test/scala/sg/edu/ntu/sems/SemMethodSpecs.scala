package sg.edu.ntu.sems

import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.testfixtures.CodeToCpgSuite

class SemMethodSpecs extends CodeToCpgSuite {

  override val code: String =
    """inline int foo(int x) {
      |  if (x>0) {
      |    return 1 + foo(x-1);
      |  } else {
      |  return 0;
      |  }
      |}
      |void _dump(char* str) {
      |  puts(str);
      |}
      |void compute(int m, int n, int** arr) {
      |while(m > n) {
      |  m--;
      |}
      |for(int i=0; i < m; i++) {
      |  for (int j=0; j < n; j++) {
      |    arr[i][j] = i * j;
      |  }
      | }
      |}
      |int main(int argc, char** argv) {
      |  if (argc > 3) {
      |  foo(argc);
      | } else {
      |   foo(argc + 3);
      | }
      | return 0;
      |}
      |""".stripMargin

  "foo method" in {
    val fooMethod: Method = cpg.method("foo").head
    MethodWrapper.isInline(fooMethod) shouldBe true
    MethodWrapper.isSelfRecursive(fooMethod) shouldBe true

    val semMethod = SemMethod(fooMethod)
    semMethod.slocOpt shouldBe (Some(7))
    semMethod.icallees.map(_.name) shouldBe Set("foo")
    semMethod.icallers.map(_.name) shouldBe Set("main", "foo")
  }

  "_dump method" in {
    val dumpMethod: Method = cpg.method("_dump").head
    MethodWrapper.isInternal(dumpMethod) shouldBe true
    MethodWrapper.isSmall(dumpMethod) shouldBe true
    val semMethod = SemMethod(dumpMethod)
    semMethod.slocOpt shouldBe Some(3)
  }

  "compute method" in {
    val computeMethod = cpg.method("compute").head
    val semCompute = SemMethod(computeMethod)
    semCompute.branches.length shouldBe 0
    semCompute.loopDepth shouldBe 2
  }

  "main method" in {
    val mainMethod = cpg.method("main").head()
    val semMain = SemMethod(mainMethod)

    semMain.cfgEdges.length shouldBe 7
    semMain.cfgNodes.length shouldBe 5
    val controls = semMain.controls
    controls.length shouldBe 2
    val control = controls.head
    control.parserTypeName shouldBe "IfStatement"
    semMain.branches.length shouldBe 2
    semMain.loops shouldBe List.empty
    semMain.loopDepth shouldBe 0

    semMain.isRecursive shouldBe false
    semMain.icallers shouldBe Set.empty
    semMain.icallees.map(_.name) shouldBe Set("foo")
    semMain.sCall.kernelUserCallees shouldBe Set.empty
    semMain.sCall.syscallsCallees shouldBe Set.empty
    semMain.sCall.stdlibCallees shouldBe Set.empty

    semMain.asMethodFeatures shouldBe (Array(8 / 15.0, 7 / 3.0, 5.0 / 3.0, 4.0, 1.0, 2 / 3.0, 0, 0, 0, 1.0, 0, 0))

  }

}
