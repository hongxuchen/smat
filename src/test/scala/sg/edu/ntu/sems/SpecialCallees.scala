package sg.edu.ntu.sems

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SpecialCalleesSpec extends AnyFlatSpec with Matchers {

  val code =
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


  "Special callees" should "be handled correctly" in {


  }

}
