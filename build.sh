#!/bin/bash

# You may need to change python3 to python for your system.
command -v python3 >/dev/null 2>&1 || { echo "Python not found." >&2; exit 1; }

LD_LIBRARY_PATH=./tool/lib python3 tool $@
