#!/usr/bin/env bash
# Regenera docs/iB2B_RA03_FINAL.asta a partir de docs/base_RA02.asta usando a API do Astah instalado.
# Uso (Mac): ./gerar.sh   [ASTAH="/Applications/astah professional/astah professional.app/Contents/Java"]
set -euo pipefail
cd "$(dirname "$0")"
ASTAH="${ASTAH:-/Applications/astah professional/astah professional.app/Contents/Java}"
CP="$ASTAH/astah-api.jar:$ASTAH/astah-pro.jar:$ASTAH/lib/*:$ASTAH/lib/batik/*:$ASTAH/lib/rlm/*"
mkdir -p classes
javac -encoding UTF-8 -cp "$CP" -d classes Astah.java GerarRA03.java
java -Djava.awt.headless=true -cp "classes:$CP" GerarRA03 ../base_RA02.asta ../iB2B_RA03_FINAL.asta
# Conferência visual sem abrir o programa (PNG de cada diagrama em ./img):
java -Dcheck_jvm_version=false -Djava.awt.headless=true -cp "$CP" \
  com.change_vision.jude.cmdline.JudeCommandRunner -image all -f ../iB2B_RA03_FINAL.asta -t png -o img
