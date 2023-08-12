@echo off

set MBT=env\bin\MaterialBinTool-0.8.1-native-image.exe
set SHADERC=env\bin\shaderc.exe

set MBT_ARGS=--compile --shaderc %SHADERC% --include include/

set DATA_DIR=data
set BUILD_DIR=build
set MATERIALS_DIR=materials

set MATERIALS=
set TARGETS=
set ARG_MODE=
:loop_args
  if "%1" == "" goto :end_args
  if "%1" == "-p" goto :set_arg
  if "%1" == "-t" goto :set_arg
  if "%1" == "-m" goto :set_arg

  if "%ARG_MODE%" == "" (
    if "%1" == "deferred" set MATERIALS_DIR=materials-deferred
    goto :next_arg
  )
  if "%ARG_MODE%" == "-p" (
    set TARGETS=%TARGETS% %1
    goto :next_arg
  )
  if "%ARG_MODE%" == "-m" (
    set MATERIALS=%MATERIALS% %MATERIALS_DIR%\%1
    goto :next_arg
  )
  if "%ARG_MODE%" == "-t" (
    set THREADS=%1
    goto :next_arg
  )
:set_arg
    set ARG_MODE=%1
:next_arg
    shift
    goto :loop_args
:end_args

if "%TARGETS%" == "" (
  set TARGETS=windows
)

if "%MATERIALS%" == "" (
  set MATERIALS=%MATERIALS_DIR%\*
)

if "%THREADS%" == "" (
  set THREADS=%NUMBER_OF_PROCESSORS%
)

set MBT_ARGS=%MBT_ARGS% --threads %THREADS%

for %%f in (%MBT%) do echo %%~nxf 
for %%p in (%TARGETS%) do (
  echo ---------------------------
  if exist %DATA_DIR%\%%p (
    echo Building materials: target=%%p

    for /d %%s in (%MATERIALS%) do (
      echo.
      echo  - %%s
      %MBT% %MBT_ARGS% --output %BUILD_DIR%\%%p --data %DATA_DIR%\%%p\%%~nxs %%s
    )

  ) else (
    echo Build aborted for %%p: %DATA%\%%p not found
  )
)
