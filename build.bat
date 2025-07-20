@echo off

where /q python || (echo Python not found. & exit /b %errorlevel%)

python tool %*