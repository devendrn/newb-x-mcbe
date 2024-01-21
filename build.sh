#!/bin/bash

MBT_JAR_FILES=(env/jar/MaterialBinTool-0.*.jar)
MBT_JAR="java -jar ${MBT_JAR_FILES[0]}"

SHADERC=env/bin/shaderc
LIB_DIR=env/lib

MBT_ARGS="--compile --shaderc $SHADERC --include include/"

DATA_VER="1.20.10"
DATA_DIR=data/$DATA_VER
BUILD_DIR=build
MATERIAL_DIR=materials

TARGETS=""
MATERIALS=""

ARG_MODE=""
for t in "$@"; do
  if [ "${t:0:1}" == "-" ]; then
    # mode
    OPT=${t:1}
    if [[ "$OPT" =~ ^[pmt]$ ]]; then
      ARG_MODE=$OPT
    else
      echo "Invalid option: $t"      
      exit 1
    fi
  elif [ "$ARG_MODE" == "p" ]; then
    # target platform
    TARGETS+="$t "
  elif [ "$ARG_MODE" == "m" ]; then
    # material files
    MATERIALS+="$MATERIAL_DIR/$t "
  elif [ "$ARG_MODE" == "t" ]; then
    # mbt threads
    THREADS="$t"
  fi
  shift
done

if [ -z "$TARGETS" ]; then
  TARGETS="android"
fi

if [ -z "$MATERIALS" ]; then
  # all materials
  MATERIALS="$MATERIAL_DIR/*"
fi

if [ -z "$THREADS" ]; then
  # 1 thread per core
  THREADS=$(nproc --all)
fi

MBT_ARGS+=" --threads $THREADS"

echo "${MBT_JAR##*/}"
for p in $TARGETS; do
  echo "----------------------------------------------"
  echo ">> Building materials - $p $DATA_VER:"
  if [ -d "$DATA_DIR/$p" ]; then
    for s in $MATERIALS; do
      echo " - $s"
      LD_LIBRARY_PATH=$LIB_DIR $MBT_JAR $MBT_ARGS --output $BUILD_DIR/$p --data $DATA_DIR/$p/${s##*/} $s -m
    done
  else
    echo "Error: $DATA_DIR/$p not found"
  fi
done
