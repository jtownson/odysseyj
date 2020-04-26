#!/usr/bin/env bash
set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
pushd "$dir" > /dev/null
java -cp ./target/odysseyj-1.0-SNAPSHOT-jar-with-dependencies.jar net.jtownson.odysseyj.VCP -t VerifiablePresentation "$@"
popd > /dev/null
