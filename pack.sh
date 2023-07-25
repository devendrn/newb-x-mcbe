#!/bin/bash

BUILD_DIR="./build/android"
BUILD_SCRIPT="./build.sh -p android -t 4"
PACK_DIR="./pack"
TEMP_PACK_DIR="$BUILD_DIR/temp"

# format:
# pack.sh 15b3 "Custom description if any"
# requires sed(for editing), jq(for prettyjson), zip(for packing)

VERSION="[0, 14, 0]"
BUILD_CODE=
CUSTOM=
ARGS_COUNT=0
VER=
for t in "$@"; do
    if [ $ARGS_COUNT == 0 ]; then
        BUILD_CODE="$t"
        VER=(${t:0:2} ${t:3:5})
        if [ -n "${VER[1]}" ]; then
            VER=($((${VER[0]}-1)) $((${VER[1]})))
        else
            VER=($((${VER[0]})) 0)
        fi
    else
        CUSTOM=$t
    fi
    ARGS_COUNT=$(($ARGS_COUNT+1))
done

echo -e ">> CREATE PACK FOLDER:\n   - $TEMP_PACK_DIR"
mkdir -p $TEMP_PACK_DIR
mkdir -p $TEMP_PACK_DIR/renderer/materials
cp -rf $PACK_DIR/* $TEMP_PACK_DIR

echo ">> UPDATE MANIFEST"
VERSION="[0, ${VER[0]}, ${VER[1]}]"
MANIFEST=$TEMP_PACK_DIR/manifest.json
if [ -n "$CUSTOM" ]; then
	sed -i "s/- all/- $CUSTOM/" $MANIFEST
    BUILD_CODE="${BUILD_CODE}c"
fi
PATTERN="*\([0-9]*\), *\([0-9]*\), *\([0-9]*\)"
sed -i "s/\"version\": \[$PATTERN\]/\"version\": $VERSION/g" $MANIFEST
sed -i "s/build-\%v-\%p/build-$BUILD_CODE/g" $MANIFEST
echo "   - code: $BUILD_CODE"
echo "   - version: $VERSION"
echo "   - custom: $CUSTOM"

echo ">> BUILD MATERIALS"
$BUILD_SCRIPT -m RenderChunk Clouds Sky EndSky LegacyCubemap Actor
mv -f $BUILD_DIR/*.material.bin $TEMP_PACK_DIR/renderer/materials/

echo ">> BUILD SUBPACKS"
SUBPACK_OPTIONS=(default no_fog no_wave_no_fog chunk_anim)
SUBPACK_NAMES=("Default" "No fog" "No wave, No fog" "Chunk loading animation")
SUBPACK_COUNT=${#SUBPACK_OPTIONS[@]}

sed -i "s/\"metadata/\"subpacks\": [],\"metadata/" $MANIFEST
for ((s=0;s<$SUBPACK_COUNT;s+=1)); do
	OPTION=${SUBPACK_OPTIONS[s]}
    echo -n "   - ${OPTION^^}: "
    sed -i "1s/^#define.*/#define ${OPTION^^}/" include/config.h

    STAT=($($BUILD_SCRIPT -m RenderChunk | tail -n 1))
    if [ "${STAT[0]}" == "Compiling" ]; then
        echo "done"
    fi

    S_DIR=$TEMP_PACK_DIR/subpacks/$OPTION/renderer/materials
    mkdir -p $S_DIR

    mv $BUILD_DIR/RenderChunk.material.bin $S_DIR

    CONTENT="{\"folder_name\": \"$OPTION\",\"name\": \"${SUBPACK_NAMES[s]}\",\"memory_tier\": 1}"
	sed -i "s/subpacks\": \[/subpacks\": \[$CONTENT,/" $MANIFEST
done

sed -i "1s/^#define.*/#define DEFAULT/" include/config.h

# remove extra comma
sed -i "s/,\],\"metadata/\],\"metadata/" $MANIFEST

# preserve \n
sed -i "s/\\\n/%eol%/g" $MANIFEST
JSON_DATA="$(jq '.' $MANIFEST)"
echo -e "${JSON_DATA}" > $MANIFEST
sed -i "s/%eol%/\\\n/g" $MANIFEST

cd $TEMP_PACK_DIR
ZIP_FILE="newb-x-$BUILD_CODE-android.mcpack"
rm -f $ZIP_FILE
zip -rq9 ../$ZIP_FILE ./* && echo -e ">> PACKED ZIP\n   - $ZIP_FILE"
