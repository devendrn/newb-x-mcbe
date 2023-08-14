#!/bin/bash

# format:
# pack.sh -v 15b3 -m "Custom description (optional)"
# if using bash on windows, do:
# pack.sh win -v 15b3 -m "Custom description" -p windows
# pack.sh win -v 15b3 -m "Custom description" -p android
# requires sed(for editing), zip(for packing)

BUILD_SCRIPT="./build.sh"
PACK_DIR="pack"
CONFIG_FILE="include/newb_config_legacy.h"
PLATFORM=android

BUILD_CODE="14"
VERSION="[0, $BUILD_CODE, 0]"
CUSTOM=
ARG_MODE=""
WIN_BASH=false
for t in "$@"; do
  if [ "$t" == "-v" ] || [ "$t" == "-m" ] || [ "$t" == "-p" ]; then
    # mode
    ARG_MODE="$t"
    continue
  elif [ "$ARG_MODE" == "-v" ]; then
    # build code
      BUILD_CODE="$t"
      VER=(${BUILD_CODE:0:2} ${BUILD_CODE:3:5})
      if [ -n "${VER[1]}" ]; then
        VERSION="[0, $((${VER[0]}-1)), ${VER[1]}]"
      else
        VERSION="[0, ${VER[0]}, 0]"
      fi
  elif [ "$ARG_MODE" == "-m" ]; then
    # custom message
    CUSTOM="'$t'"
    BUILD_CODE="${BUILD_CODE}c"
  elif [ "$ARG_MODE" == "-p" ]; then
    # platform
    PLATFORM="$t"
  elif [ -z "$ARG_MODE" ]; then
    # using bash on win
    if [ "$t" == "win" ]; then
      BUILD_SCRIPT="./build.bat"
      WIN_BASH=true
    fi
  fi
  ARG_MODE=
  shift
done

BUILD_SCRIPT="$BUILD_SCRIPT -p $PLATFORM"
BUILD_DIR="build/$PLATFORM"
TEMP_PACK_DIR="$BUILD_DIR/temp"
MANIFEST="$TEMP_PACK_DIR/manifest.json"

echo -e ">> CREATE PACK FOLDER:\n   - $TEMP_PACK_DIR"
mkdir -p $TEMP_PACK_DIR
mkdir -p $TEMP_PACK_DIR/renderer/materials
cp -rf $PACK_DIR/* $TEMP_PACK_DIR

echo ">> UPDATE MANIFEST"
if [ "$PLATFORM" == "windows" ]; then
  sed -i "s/\%w/Only works with BetterRenderDragon/" $MANIFEST
else
  sed -i "s/\%w/Only works with Patched Minecraft/" $MANIFEST
fi
sed -i "s/\%c/$CUSTOM/" $MANIFEST
sed -i "s/\"version\": \[.*]/\"version\": $VERSION/g" $MANIFEST
sed -i "s/\%v-\%p/$BUILD_CODE-$PLATFORM/g" $MANIFEST
echo "   - code: $BUILD_CODE-$PLATFORM"
echo "   - version: $VERSION"
echo "   - custom: $CUSTOM"

echo ">> BUILD MATERIALS"
$BUILD_SCRIPT -m RenderChunk Clouds Sky EndSky LegacyCubemap
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

if [ $WIN_BASH = false ]; then
  cd $TEMP_PACK_DIR
  ZIP_FILE="newb-x-$BUILD_CODE-$PLATFORM.mcpack"
  rm -f $ZIP_FILE
  zip -rq9 ../$ZIP_FILE ./* && echo -e ">> PACKED ZIP\n   - $ZIP_FILE"
fi
