#!/bin/bash

MBT_VERSION="0.8.1"
MBT_JAR=env/jar/MaterialBinTool-$MBT_VERSION-all.jar
SHADERC=env/bin/shaderc
DATA_VER="1.20.10"
DATA_DIR=data/$DATA_VER

MBT_JAR_URL="https://github.com/ddf8196/MaterialBinTool/releases/download/v$MBT_VERSION/MaterialBinTool-$MBT_VERSION-all.jar"
M_DATA_URL="https://cdn.discordapp.com/attachments/1137039470441550004/1178732085628915803/materials-data-$DATA_VER-few.zip"

CPU_ARCH=$(uname -m)
if [ $CPU_ARCH == "x86_64" ]; then
  CPU_ARCH="x86_64"
elif [ $CPU_ARCH == "aarch64" ]; then
  CPU_ARCH="arm64"
elif [ $CPU_ARCH == "armv7l" ] || [ $CPU_ARCH == "armv8l" ]; then
  CPU_ARCH="arm32"
else
  echo "Error: No shaderc binary found for $CPU_ARCH"
  exit 1;
fi
SHADERC_URL="https://github.com/devendrn/RenderDragonSourceCodeInv/releases/download/v1/shaderc.$CPU_ARCH"

if [ "$1" == "-f" ]; then
  # clean
  rm -rf env data build
fi

if [ ! -f "$MBT_JAR" ]; then
  mkdir -p env/jar
  echo "Downloading MaterialBinTool-$MBT_VERSION-all.jar"
  curl -Lo $MBT_JAR $MBT_JAR_URL
fi

if [ ! -f "$SHADERC" ]; then
  mkdir -p env/bin
  echo "Downloading shaderc $CPU_ARCH"
  curl -Lo $SHADERC $SHADERC_URL
  chmod +x $SHADERC
fi

# libc++_shared.so not found fix for termux
TERMUX_FILES="/data/data/com.termux/files"
if [ -d "$TERMUX_FILES" ] && [ ! -f "env/lib/libc++_shared.so" ]; then
  echo "Termux fix: libc++_shared.so"
  mkdir -p env/lib
  cp $TERMUX_FILES/usr/lib/libc++_shared.so env/lib
fi

if [ ! -d "$DATA_DIR" ]; then
  mkdir -p $DATA_DIR
  echo "Downloading materials-data-$DATA_VER.zip"
  curl -Lo $DATA_DIR/temp.zip $M_DATA_URL
  echo "Extracting materials-data-$DATA_VER.zip"
  unzip -qd $DATA_DIR/ $DATA_DIR/temp.zip
  rm $DATA_DIR/temp.zip
fi

if [ "$1" == "-u" ]; then
  echo "Unpacking:"
  MB_DIRS=$DATA_DIR/*
  for i in $MB_DIRS; do
    echo -e "\n> $i"
    java -jar $MBT_JAR --unpack --data-only $i/*.material.bin
  done
fi
