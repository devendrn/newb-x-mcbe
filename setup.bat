@echo off

set MBT_VERSION=0.8.1
set MBT=env\bin\MaterialBinTool-%MBT_VERSION%-native-image.exe
set SHADERC=env\bin\shaderc.exe
set DATA_VER=1.20.10
set DATA_DIR=data\%DATA_VER%

set MBT_RELEASE_URL=https://github.com/ddf8196/MaterialBinTool/releases/download/v%MBT_VERSION%
set M_DATA_HTTP=https://cdn.discordapp.com/attachments/1137039470441550004/1178732085628915803/materials-data-%DATA_VER%-few.zip

if not exist %MBT% (
  mkdir env\bin\
  echo Downloading MaterialBinTool-%MBT_VERSION%-native-image.exe
  curl -L -o %MBT% %MBT_RELEASE_URL%/MaterialBinTool-%MBT_VERSION%-native-image.exe
)

if not exist %SHADERC% (
  echo Downloading shaderc.exe
  curl -L -o %SHADERC% %MBT_RELEASE_URL%/shaderc.exe
)

if not exist %DATA_DIR% (
  mkdir %DATA_DIR%
  echo Downloading materials-data-%DATA_VER%.zip
  curl -L -o %DATA_DIR%/temp.zip %M_DATA_HTTP%
  echo Extracting materials-data-%DATA_VER%.zip
  powershell Expand-Archive %DATA_DIR%\temp.zip -DestinationPath %DATA_DIR%
  del %DATA_DIR%\temp.zip
)
