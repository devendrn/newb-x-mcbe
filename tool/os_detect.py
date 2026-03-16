import fnmatch
import platform
import os

if platform.system() == "Darwin":
    if fnmatch.fnmatch(platform.machine(), "iP*"):
        print("ios")
    else:
        print("macos")
elif platform.system() == "Linux":
    print("linux")
elif platform.system() == "Android":
    print("android")
elif platform.system() == "Windows":
    print("windows")
else:
    print("other")
