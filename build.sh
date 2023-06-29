#!/bin/bash

MBT_JAR=env/jar/MaterialBinTool-0.8.*.jar
SHADERC=env/bin/shaderc

MBT_ARGS="--compile --shaderc $SHADERC --include include/ --threads 2"

DATA_DIR=data
BUILD_DIR=build
MATERIAL_DIR=materials

# android windows ios"
TARGETS=""

MATERIALS=""

ARGS=("$@")
for t in "${ARGS[@]}"; do
  if [ "$t" == "android" ] || [ "$t" == "windows" ] || [ "$t" == "ios" ] ; then
    TARGETS+="$t "
  else
    MATERIALS+="$MATERIAL_DIR/$t "
  fi
  shift
done

for p in $TARGETS
do
  echo "----------------------------------------------"
  if [ -d "$DATA_DIR/$p" ]; then
    echo "Building materials: target=$p"

    if [ -z $MATERIALS ]; then
      # all materials
      MATERIALS=$MATERIAL_DIR/*
    fi

    for s in $MATERIALS
    do
      echo
      echo " - $s"
      java -jar $MBT_JAR $MBT_ARGS --output $BUILD_DIR/$p --data $DATA_DIR/$p/${s##*/} $s
    done

  else
    echo "Build aborted for $p: $DATA_DIR/$p not found"
  fi
done
