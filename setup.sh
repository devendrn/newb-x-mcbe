#!/bin/bash

MBT_VERSION="0.8.1"
MBT_JAR=env/jar/MaterialBinTool-$MBT_VERSION-all.jar
SHADERC=env/bin/shaderc
DATA_DIR=data

MBT_JAR_URL="https://github.com/ddf8196/MaterialBinTool/releases/download/v$MBT_VERSION/MaterialBinTool-$MBT_VERSION-all.jar"
M_DATA_URL="https://cdn.discordapp.com/attachments/1131575028329222154/1136034200949104801/materials-data-1.20-few.zip"

SHADERC_URL=
CPU_ARCH=$(uname -m)
if [ $CPU_ARCH == "x86_64" ]; then
  SHADERC_URL="https://cdn.discordapp.com/attachments/1137039470441550004/1137070225838317639/shaderc.x86_64"
elif [ $CPU_ARCH == "aarch64" ]; then
  SHADERC_URL="https://cdn.discordapp.com/attachments/1137039470441550004/1137070225502765086/shaderc.arm64"
elif [ $CPU_ARCH == "armv7l" ]; then
  SHADERC_URL="https://cdn.discordapp.com/attachments/1137039470441550004/1137070225095925931/shaderc.arm32"
else
  echo "Cannot setup build environment for $CPU_ARCH"
  exit 0;
fi

if [ ! -f "$MBT_JAR" ]; then
  mkdir -p env/jar/
  echo "Downloading MaterialBinTool-$MBT_VERSION-all.jar"
  curl -Lo $MBT_JAR $MBT_JAR_URL
fi

if [ ! -f "$SHADERC" ]; then
  mkdir -p env/bin/
  echo "Downloading shaderc x86_64"
  curl -Lo $SHADERC $SHADERC_x86_64_URL
  chmod +x $SHADERC
fi

if [ ! -d "$DATA_DIR" ]; then
  mkdir -p $DATA_DIR
  echo "Downloading materials-data.zip"
  curl -Lo $DATA_DIR/temp.zip $M_DATA_URL
  echo "Extracting materials-data.zip"
  unzip -d $DATA_DIR/ $DATA_DIR/temp.zip
  rm $DATA_DIR/temp.zip
fi

if [ "$1" == "-u" ]; then
  echo "Unpacking:"
  MB_DIRS=$DATA_DIR/*
  for i in $MB_DIRS; do
    echo
    echo "> $i"
    #java -jar $MBT_JAR --unpack --data-only $i/*.material.bin
    java -jar $MBT_JAR --unpack $i/*.material.bin
  done
  exit
fi
