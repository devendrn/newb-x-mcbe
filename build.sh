#!/bin/bash
PLATFORM=$(python ./tool/os_detect.py)

case "$PLATFORM" in
    "android")
        if [ ! -f "./tool/lib/libc++_shared.so" ]; then
            echo "./tool/lib/libc++_shared.so not found!"
            echo "fix lib for android"
            cp /data/data/com.termux/files/usr/lib/libc++_shared.so ./tool/lib/libc++_shared.so
        fi
    ;;

    "other")
        echo "Error: Operating system '$PLATFORM' is not explicitly supported for setup!" >&2
        exit 1
    ;;
esac

LD_LIBRARY_PATH=./tool/lib python3 tool $@
