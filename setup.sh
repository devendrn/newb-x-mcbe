#!/bin/bash

MBT_JAR=env/jar/MaterialBinTool-*.jar
MB_DIRS=data/*

for i in $MB_DIRS
do
  java -jar $MBT_JAR --unpack $MB_DIRS
done
