@echo off

set MBT="env\bin\MaterialBinTool-0.8.0-native-image.exe"
set SHADERC=env\bin\shaderc.exe

set MBT_THREADS=2
set MBT_ARGS=--compile --shaderc %SHADERC% --include include/ --threads %MBT_THREADS%

set DATA_DIR=data
set BUILD_DIR=build
set MATERIAL_DIR=materials

set TARGETS=%*

for %%p in (%TARGETS%) do (
	echo ---------------------------
	if exist %DATA_DIR%\%%p (
		echo Building materials: target=%%p

		for /d %%s in (%MATERIAL_DIR%\*) do (
			echo.
			echo  - %%s
			%MBT% %MBT_ARGS% --output %BUILD_DIR%\%%p --data %DATA_DIR%\%%p\%%~nxs %%s
		)

	) else (
		echo Build aborted for %%p: %DATA%\%%p not found
	)
)
