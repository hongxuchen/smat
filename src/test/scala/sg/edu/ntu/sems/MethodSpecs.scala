package sg.edu.ntu.sems

import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.testfixtures.CodeToCpgSuite

class MethodSpecs extends CodeToCpgSuite {

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
      |""".stripMargin

  "interesting functions should be detected" in {
    val fooMethod = cpg.method("foo").head
    MethodWrapper.isInline(fooMethod) shouldBe true
    MethodWrapper.isSelfRecursive(fooMethod) shouldBe true

    val dumpMethod = cpg.method("_dump").head
    MethodWrapper.isInternal(dumpMethod) shouldBe true
    MethodWrapper.isSmall(dumpMethod) shouldBe true
  }

  ""

}
