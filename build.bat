@echo off

where /q python || (echo Python not found. & exit /b 1)

python tool %*
