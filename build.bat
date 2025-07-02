@echo off

where /q python || echo Python not found. & goto :EOF

python tool %*
