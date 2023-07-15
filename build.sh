#!/bin/bash

MBT_JAR_FILES=(env/jar/MaterialBinTool-0.*.jar)
MBT_JAR="${MBT_JAR_FILES[0]}"

SHADERC=env/bin/shaderc

MBT_ARGS="--compile --shaderc $SHADERC --include include/"

DATA_DIR=data
BUILD_DIR=build
MATERIAL_DIR=materials-legacy

TARGETS=""

MATERIALS=""

ARG_MODE=""
ARGS=("$@")
for t in "${ARGS[@]}"; do
  if [ "$t" == "-p" ] || [ "$t" == "-m" ] || [ "$t" == "-t" ]; then
    # mode
    ARG_MODE="$t"
  elif [ "$ARG_MODE" == "-p" ]; then
    # target platform
    TARGETS+="$t "
  elif [ "$ARG_MODE" == "-m" ]; then
    # material files
    MATERIALS+="$MATERIAL_DIR/$t "
  elif [ "$ARG_MODE" == "-t" ]; then
    # mbt threads
    THREADS="$t"
  elif [ -z "$ARG_MODE" ]; then
    # build main (default build legacy)
    if [ "$t" == "main" ]; then
      MATERIAL_DIR=materials
    fi
  fi
  shift
done

if [ -z "$MATERIALS" ]; then
  # all materials
  MATERIALS="$MATERIAL_DIR/*"
fi

if [ -n "$THREADS" ]; then
  MBT_ARGS+=" --threads $THREADS"
fi

echo "${MBT_JAR##*/}"
for p in $TARGETS; do
  echo "----------------------------------------------"
  if [ -d "$DATA_DIR/$p" ]; then
    echo "Building materials: target=$p"

    for s in $MATERIALS; do
      echo
      echo " - $s"
      java -jar $MBT_JAR $MBT_ARGS --output $BUILD_DIR/$p --data $DATA_DIR/$p/${s##*/} $s -m
    done

  else
    echo "Build aborted for $p: $DATA_DIR/$p not found"
  fi
done
