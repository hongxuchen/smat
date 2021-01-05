package sg.edu.ntu.sems

import io.shiftleft.semanticcpg.language.toNodeTypeStarters
import io.shiftleft.semanticcpg.testfixtures.CodeToCpgSuite

class SpecialCalleesSpec extends CodeToCpgSuite {

  override val code: String =
    """#include<string.h>
      |#include<stdlib.h>
      |#include<stdio.h>
      |
      |const int MYCONST = 42;
      |
      |int main(int argc, char** argv) {
      |  if (argc < 2) {
      |    fprintf(stderr, "usage: %s STRING", argv[0]);
      |  }
      |  int res =  strcmp(argv[1], "MATCH");
      |  if (res < MYCONST) {
      |    puts("of course");
      |  }
      |  return 0;
      |}""".stripMargin


  "Special callees should be handled correctly" in {
    val mainMethod = cpg.method("main").head()
    val sCall = new SpecialCall(mainMethod)
    sCall.syscallsCallees shouldBe Set.empty
    sCall.stdlibCallees shouldBe Set("fprintf", "puts", "strcmp")

  }

}
