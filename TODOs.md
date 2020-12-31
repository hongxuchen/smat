* matching scenario
  - [x] multiple projects
  - [x] multiple versions
  - [ ] specific code segments
* matching CLI should be handled similar with extraction
  - [x] cpg serialization (use existing ones)
  - [x] smdb serialization (force update, etc)
    - [x] format design
    - [ ] scala json (circe) for serialization/deserialization
  - [x] separate directory and option to specify matching projects
    - [x] component input directory
    - [x] logics to add into `cpg`/`smdb` DB
* SMDB generation
  - [x] normalization of strings
  - [x] format
    - [x] Coarse Grained
    - [x] Fine Grained
    - [x] Project Specific
* automation scripts
  - [ ] `/usr/bin/fuzzyppcli` installation script
  - [ ] scoring automation
