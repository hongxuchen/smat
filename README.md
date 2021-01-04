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
* To semantically model a module, run `./smat.sh --module=module1_1.0 /path/to/module_1.0`, where `module1` is typically a module in a project with a specific release version (1.0). For now, we require the input module name to be of the format "[A-Za-z_]+_[0-9]+.[0-9]+". Afterwards, there will be `module_1.0.raw` ([code property graph](https://docs.shiftleft.io/core-concepts/code-property-graph)) and `module_1.0.sm` (our semantic model) inside `SMDB` directory.
* To do the semantic match, given a module `moduleT_1.9`, run `./smat.sh --module=moduleT_1.9 -M` to generate `moduleT_1.9.sm` to match with the top-N similar resulting modules.


Experimental Setup
=================
During evaluation, we resort to `openssl`, `libressl`, `gnutls` and `libjpeg-turbo`, `mozjpeg` for semantic
model and additionally `boringssl` for semantic matching.

The modules used in semantic modeling phrase are generated in two steps:
* `python ./scripts/vers_from_gits.py -i ../indir -o ../outdir` to get the different release versions
 of the corresponding projects. A json file `records.json` will be created when it succeeds.
* `python ./scripts/modules_from_projs.py -i ./records.json ../modules` (or `python ./scripts/modules_from_projs.py -i ../outdir -o ../modules`) to get the "modules" which are
based on directory structures -- since this is only for proof of our matching work,
 no fantastic [clustering algorithms](https://www.cs.purdue.edu/homes/lintan/publications/archrec-tse17.pdf) are applied.


## Repos
* openssl: https://github.com/openssl/openssl.git
* boringssl: https://github.com/google/boringssl.git
* gnutls: https://github.com/gnutls/gnutls.git (w/o devel/openssl)
* libressl: https://github.com/libressl-portable/openbsd.git (only src/lib)
* libjpeg-turbo: https://github.com/libjpeg-turbo/libjpeg-turbo.git
* mozjpeg: https://github.com/mozilla/mozjpeg.git

## Semantic Modeling

The modules used in semantic modeling phrase are generated in two steps:
* `python ./scripts/vers_from_gits.py -i ../indir -o ../outdir` to get the different release versions
 of the corresponding projects. A json file `records.json` will be created when it succeeds.
* `python ./scripts/modules_from_projs.py -i ./records.json ../modules` (or `python ./scripts/modules_from_projs.py -i ../outdir -o ../modules`) to get the "modules" which are
based on directory structures -- since this is only for proof of our matching work,
 no fantastic [clustering algorithms](https://www.cs.purdue.edu/homes/lintan/publications/archrec-tse17.pdf) are applied.

## Semantic Matching
* The modules for matching purpose come from `openssl`, `libressl`, `gnutls` and `libjpeg-turbo`, `mozjpeg` and `boringssl`. We deliberately choose releases that are NONE of the
commits from the modeled versions (with git tags). The generation of these versions are done manually, and modules are extracted similarly with the help of `modules_from_projs.py`.
* Run `./smat.sh --module=xxx_xxx module_dir -M` to get the matching results.
