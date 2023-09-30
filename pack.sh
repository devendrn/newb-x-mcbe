#!/bin/bash

# pack.sh usage:
# - linux: (android pack only)
#     pack.sh -v 15.0 -m "Custom name (optional)"
# - bash on windows: (windows and android pack)
#     pack.sh win -v 15.0 -m "Custom name" -p windows
#     pack.sh win -v 15.0 -m "Custom name" -p android

# materials to compile for default
DEFAULT_MATERIALS="RenderChunk Clouds Sky EndSky LegacyCubemap Actor"

# subpacks:
#  OPTIONS   = subpack options
#  NAMES     = names/descriptions for options
#  MATERIALS = materials to compile for options
SUBPACK_OPTIONS=(
  ROUNDED_CLOUDS
  CHUNK_ANIM
  NO_WAVE_NO_FOG
  NO_FOG
  NO_WAVE
  DEFAULT
)
SUBPACK_NAMES=(
  "Rounded Clouds"
  "Chunk loading animation"
  "No wave, No fog"
  "No fog"
  "No wave"
  "Default"
)
SUBPACK_MATERIALS=(
  "Clouds"
  "RenderChunk"
  "RenderChunk"
  "RenderChunk"
  "RenderChunk"
  ""
)

BUILD_SCRIPT="./build.sh"
PACK_DIR="pack"
CONFIG_FILE="include/newb_config_legacy.h"
PLATFORM=android

# version format: tag.commits
VERSION=15.0
if command -v git &> /dev/null; then
  GIT_TAG=$(git describe --tags)
  GIT_TAG=${GIT_TAG/v/}
  GIT_TAG=(${GIT_TAG//-/ })
  if [ ${#GIT_TAG[*]} == 3 ]; then
    VERSION=${GIT_TAG[0]}.${GIT_TAG[1]}
  elif [ -n "${GIT_TAG[0]}" ]; then
    VERSION=${GIT_TAG[0]}.0
  fi
fi

CUSTOM=
ARG_MODE=""
for t in "$@"; do
  if [ "$t" == "-v" ] || [ "$t" == "-m" ] || [ "$t" == "-p" ]; then
    ARG_MODE="$t"
    continue
  elif [ "$ARG_MODE" == "-v" ]; then
    VERSION="$t"
  elif [ "$ARG_MODE" == "-m" ]; then
    CUSTOM="$t"
  elif [ "$ARG_MODE" == "-p" ]; then
    PLATFORM="$t"
  elif [ -z "$ARG_MODE" ]; then
    # using bash on win
    if [ "$t" == "win" ]; then
      BUILD_SCRIPT="./build.bat"
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
mkdir -p $TEMP_PACK_DIR/renderer/materials
cp -rf $PACK_DIR/* $TEMP_PACK_DIR

echo ">> UPDATE MANIFEST"
if [ "$PLATFORM" == "windows" ]; then
  sed -i "s/\%w/Only works with BetterRenderDragon/" $MANIFEST
else
  sed -i "s/\%w/Only works with Patched Minecraft/" $MANIFEST
fi
if [ -z "$CUSTOM" ]; then
  sed -i "s/\%c//" $MANIFEST
else
  sed -i "s/\%c/- §b$CUSTOM/" $MANIFEST
fi
sed -i "s/\"version\": \[.*]/\"version\": [0, ${VERSION/./, }]/g" $MANIFEST
sed -i "s/\%v/v$VERSION ${PLATFORM^}/g" $MANIFEST
echo -e "   - platform: $PLATFORM\n   - version: 0.$VERSION\n   - custom name: $CUSTOM"

echo ">> BUILD MATERIALS"
rm -f $BUILD_DIR/*.material.bin
$BUILD_SCRIPT -m $DEFAULT_MATERIALS
mv -f $BUILD_DIR/*.material.bin $TEMP_PACK_DIR/renderer/materials/

echo ">> BUILD SUBPACKS"
SUBPACK_COUNT=${#SUBPACK_OPTIONS[@]}
CONTENT=
for ((s=0; s<$SUBPACK_COUNT; s+=1)); do
  OPTION=${SUBPACK_OPTIONS[s]}
  S_MATS=${SUBPACK_MATERIALS[s]}
  S_DIR=$TEMP_PACK_DIR/subpacks/${OPTION,,}/renderer/materials
  mkdir -p $S_DIR
  echo -n "   - ${OPTION}: "

  if [ -z "$S_MATS" ]; then
    echo "done"
  else
    sed -i "3s/.*/#define $OPTION/" $CONFIG_FILE
    STAT=($($BUILD_SCRIPT -m $S_MATS | tail -n 1))
    if [ "${STAT[0]}" == "Compiling" ]; then
      mv $BUILD_DIR/*.material.bin $S_DIR
      echo "done"
    else
      echo "failed"
    fi
  fi

  # quote special chars used by sed
  DESCRIPTION="$(<<< "${SUBPACK_NAMES[s]}" sed -e 's`[][\\/.*^$]`\\&`g')"

  CONTENT="$CONTENT        {\"folder_name\": \"${OPTION,,}\", \"name\": \"$DESCRIPTION\", \"memory_tier\": 1},\n"
done

sed -i "s/\"metadata/\"subpacks\": [\n${CONTENT%,*}\n     ],\n    \"metadata/" $MANIFEST
sed -i "3s/.*/\/\/ line 3 reserved/" $CONFIG_FILE

# pack if zip exists
if command -v zip &> /dev/null; then
  cd $TEMP_PACK_DIR
  ZIP_FILE="newb-x-$VERSION-$PLATFORM.mcpack"
  rm -f $ZIP_FILE
  zip -rq9 ../$ZIP_FILE ./* && echo -e ">> PACKED ZIP\n   - $ZIP_FILE"
fi
