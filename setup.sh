#!/bin/bash

MBT_JAR=env/jar/MaterialBinTool-*.jar
MB_DIRS=data/*

if [ -f "$MBT_JAR" ]; then
    echo "$MBT_JAR not found"
    exit 0
fi

echo "Unpacking:"

for i in $MB_DIRS; do
  echo 
  echo "> $i"
  java -jar $MBT_JAR --unpack --data-only $i/*.material.bin
done
