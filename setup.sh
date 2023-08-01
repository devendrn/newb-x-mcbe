#!/bin/bash

MBT_VERSION="0.8.1"
MBT_JAR=env/jar/MaterialBinTool-$MBT_VERSION-all.jar
SHADERC=env/bin/shaderc
DATA_DIR=data

MBT_JAR_URL="https://github.com/ddf8196/MaterialBinTool/releases/download/v$MBT_VERSION/MaterialBinTool-$MBT_VERSION-all.jar"
SHADERC_x86_64_URL="https://cdn.discordapp.com/attachments/1131575028329222154/1131576656906170449/shaderc"
M_DATA_URL="https://cdn.discordapp.com/attachments/1131575028329222154/1136034200949104801/materials-data-1.20-few.zip"

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
    java -jar $MBT_JAR --unpack --data-only $i/*.material.bin
  done
  exit
fi
