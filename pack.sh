#!/bin/bash

BUILD_DIR="build/android"
BUILD_SCRIPT="./build.sh"
PACK_DIR="pack"
TEMP_PACK_DIR="$BUILD_DIR/temp"
CONFIG_FILE="include/newb_config_legacy.h"
MANIFEST="$TEMP_PACK_DIR/manifest.json"

# format:
# pack.sh 15b3 "Custom description if any"
# requires sed(for editing), zip(for packing)

BUILD_CODE="14"
VERSION="[0, $BUILD_CODE, 0]"
CUSTOM=
if [ -n "$1" ]; then
  BUILD_CODE="$1"
  VER=(${BUILD_CODE:0:2} ${BUILD_CODE:3:5})
  if [ -n "${VER[1]}" ]; then
    VERSION="[0, $((${VER[0]}-1)), ${VER[1]}]"
  else
    VERSION="[0, ${VER[0]}, 0]"
  fi
  if [ -n "$2" ]; then
    CUSTOM="'$2'"
    BUILD_CODE="${BUILD_CODE}c"
  fi
fi

echo -e ">> CREATE PACK FOLDER:\n   - $TEMP_PACK_DIR"
mkdir -p $TEMP_PACK_DIR
mkdir -p $TEMP_PACK_DIR/renderer/materials
cp -rf $PACK_DIR/* $TEMP_PACK_DIR

echo ">> UPDATE MANIFEST"
sed -i "s/\%c/$CUSTOM/" $MANIFEST
sed -i "s/\"version\": \[.*]/\"version\": $VERSION/g" $MANIFEST
sed -i "s/\%v-\%p/$BUILD_CODE-android/g" $MANIFEST
echo "   - code: $BUILD_CODE"
echo "   - version: $VERSION"
echo "   - custom: $CUSTOM"

echo ">> BUILD MATERIALS"
$BUILD_SCRIPT -m RenderChunk Clouds Sky EndSky LegacyCubemap Actor
mv -f $BUILD_DIR/*.material.bin $TEMP_PACK_DIR/renderer/materials/

echo ">> BUILD SUBPACKS"
SUBPACK_OPTIONS=(DEFAULT NO_FOG NO_WAVE_NO_FOG CHUNK_ANIM)
SUBPACK_NAMES=("Default" "No fog" "No wave, No fog" "Chunk loading animation")
SUBPACK_COUNT=${#SUBPACK_OPTIONS[@]}

CONTENT=
for ((s=0;s<$SUBPACK_COUNT;s+=1)); do
  OPTION=${SUBPACK_OPTIONS[s]}
  echo -n "   - ${OPTION}: "
  sed -i "3s/.*/#define $OPTION/" $CONFIG_FILE

  STAT=($($BUILD_SCRIPT -m RenderChunk | tail -n 1))
  if [ "${STAT[0]}" == "Compiling" ]; then
    echo "done"
  else
    echo "failed"
  fi

  S_DIR=$TEMP_PACK_DIR/subpacks/${OPTION,,}/renderer/materials
  mkdir -p $S_DIR
  mv $BUILD_DIR/RenderChunk.material.bin $S_DIR

  CONTENT="$CONTENT        {\"folder_name\": \"${OPTION,,}\", \"name\": \"${SUBPACK_NAMES[s]}\", \"memory_tier\": 1},\n"
done

sed -i "s/\"metadata/\"subpacks\": [\n${CONTENT%,*}\n     ],\n    \"metadata/" $MANIFEST
sed -i "3s/.*/\/\/ line 3 reserved/" $CONFIG_FILE

cd $TEMP_PACK_DIR
ZIP_FILE="newb-x-$BUILD_CODE-android.mcpack"
rm -f $ZIP_FILE
zip -rq9 ../$ZIP_FILE ./* && echo -e ">> PACKED ZIP\n   - $ZIP_FILE"
