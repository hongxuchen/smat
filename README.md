SMat
======

## TODOs
* matching scenario
  - [ ] multiple projects
  - [ ] multiple versions
  - [ ] specific code segments
* matching CLI should be handled similar with extraction
  - [x] cpg serialization (use existing ones)
  - [ ] smdb serialization (force update, etc)
    - [ ] format design
    - [ ] scala json (circe) for serialization/deserialization
  - [ ] separate directory and option to specify matching projects
    - [ ] component input directory
    - [ ] logics to add into `cpg`/`smdb` DB
* SMDB generation
  - [ ] normalization of strings
  - [ ] format
    - [ ] Coarse Grained
    - [ ] Fine Grained
    - [ ] Project Specific
