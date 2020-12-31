SMAT
======
SMAT is a tool to model a project's module-wise semantic metrics and match similarities. It is written in [Scala](https://www.scala-lang.org/) and managed by [sbt](https://www.scala-sbt.org/).

Installation
============

* [Install sbt](https://www.scala-sbt.org/1.x/docs/Setup.html);
* Run `sbt stage` to build the package (at the first time, this may take a long time); afterwards, a folder `./target/universal/stage` will be generated, where `./target/universal/stage/lib` contains packaged JARs and `.target/universal/stage/bin` contains platform-dependent executable wrapper scripts to run.
* Run `./updateSymlinks.sh` to update the symlink of the generated wrapper scripts (*NIX only). Typically, `smat.sh` will be created in the root directory.
* Run `./smat.sh --help` to get the help messages like below:
```
Usage: Smat$ [options] <src-dir>

  --help
  <src-dir>               source directories containing C/C++ code
  --module <value>        module metadata to be specified
  --force-update-cpg      force update cpg
  --force-update-smdb     force update SMDB
  --src-ext <value>       source file extensions to include when gathering source files.
  --include <value>       header include files
  -I, -- <value>          header include paths
  -D, --define <value>    define a MACRO value
  -U, --undefine <value>  undefine a MACRO value
  --pp-exe <value>        path to the preprocessor executable
  -M, --match             flag to indicate a semantic match procedure
  --score <value>         matching scoring strategy
  --help                  display this help message
```

Usage
======
* To semantically model a module, run `./smat.sh --module=module1-1.0 /path/to/module-1.0`, where `module1` is typically a module in a project with a specific release version (1.0). For now, we require the input module name to be of the format "[A-Za-z_]+-[0-9]+.[0-9]+". Afterwards, there will be `module-1.0.raw` ([code property graph](https://docs.shiftleft.io/core-concepts/code-property-graph)) and `module-1.0.sm` (our semantic model) inside `SMDB` directory.
* To do the semantic match, given a module `moduleT-1.9`, run `./smat.sh --module=moduleT-1.9 -M` to generate `moduleT-1.9.sm` to match with the top-N similar resulting modules.
