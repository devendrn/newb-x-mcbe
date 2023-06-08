#!/bin/bash

MBT_JAR=env/jar/MaterialBinTool-*.jar
SHADERC=env/bin/shaderc

MBT_THREADS=0
MBT_ARGS="--compile --shaderc $SHADERC --include include/ --threads $MBT_THREADS"

DATA_DIR=data
BUILD_DIR=build
MATERIAL_DIR=material

#TARGETS="android windows ios"
TARGETS="android"

for p in $TARGETS
do
  echo "----------------------------------------------"
  if [ -d "$DATA_DIR/$p" ]; then
    echo "Building materials: target=$p"
    for s in $MATERIAL_DIR/*
    do
      echo
      echo " - $s"
      java -jar $MBT_JAR $MBT_ARGS --output $BUILD_DIR/$p --data $DATA_DIR/$p/${s##*/} $s
    done
  else
    echo "Build aborted for $p: $DATA_DIR/$p not found"
  fi
done
