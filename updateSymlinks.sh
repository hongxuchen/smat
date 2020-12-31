#!/usr/bin/env bash

# shellcheck disable=SC2046
# shellcheck disable=SC2209

os="linux"
if [ "$(uname)" == "Darwin" ]; then
    os="darwin"
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    os="gnu-linux"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    os="mingw32"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW64_NT" ]; then
    os="mingw64"
else
    echo "unknown linux: $(uname -a)"
fi

if [[ $os == "darwin" ]]; then
    FIND=gfind
else
    FIND=find
fi

$FIND . -maxdepth 1 -xtype l -delete

targetDir="./target/universal/stage/bin"

for f in "${targetDir}"/*; do
    echo "===> $f"
    if [[ ${f: -4} == ".bat" ]]; then
        echo "SKIP: $f"
    else
        bf="$(basename $f)"
        bf_sh="${bf}.sh"
        ln -sf  "$f" "${bf_sh}"
    fi
done
