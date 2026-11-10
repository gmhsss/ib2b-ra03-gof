#!/usr/bin/env bash
# Roda os testes JUnit e depois a demonstração completa pela CLI.
set -euo pipefail
cd "$(dirname "$0")"
mvn -q clean test
mvn -q compile exec:java -Dexec.args="--demo"
