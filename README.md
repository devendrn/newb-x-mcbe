# newb-x-mcbe

Newb X Legacy is a ported version of [newb-shader-mcbe](https://github.com/devendrn/newb-shader-mcbe) for MCBE 1.20. It is a vanilla shader based on the theme "lightweight and soft aesthetics".

> Note:
This is an experimental repository, breaking changes are made often.
Also, there is no guarantee of continued development.

## Screenshot

![Screenshot1](docs/screenshot1.jpg "Newb X on MCBE 1.19.83")

## Downloads

Nightly builds for Android (ESSL) and Windows (DX) can be found at [Discord server](https://discord.gg/z9TBnq33HC).

## Installation

Linux: ([minecraft-manifest](https://github.com/minecraft-linux/mcpelauncher-ui-manifest))
- Replace material.bin files inside data root (backup original files first).
- Import resource pack and activate it in global resources.

Windows:
- Use [BetterRenderDragon](https://github.com/ddf8196/BetterRenderDragon) to load material.bin files via resource pack.


## Building

### Linux

**Prerequisites:**
- [MaterialBinTool-0.8.x-all.jar](https://github.com/ddf8196/MaterialBinTool)
- shaderc (from bgfx-mcbe)

You can find a precompiled binary (x86_64) for bgfx-mcbe shaderc at [Discord server](https://discord.gg/z9TBnq33HC).

Set up the directory structure as follows:
```
├── data/
│   └── android/            <platform>
│       └── *.material.bin  <vanilla mb files>
├── env/
│   ├── bin/
│   │   └── shaderc
│   └── jar/
│       └── MaterialBinTool-0.8.x-all.jar
├── include/
├── materials/
├── README.md
├── build.sh
└── setup.sh
```

1. Place the vanilla material.bin files (required ones only) inside `data/<platform>/`.
2. Unpack the vanilla material.bin files:
```
./setup.sh
```
3. Compile the material src files:
```
./build.sh -p android
```
Available parameters for `build.sh` are:
```
-p    Target platforms (android,ios)
-m    Materials to compile (if unspecified, builds all material files)
-t    Number of threads to use for compilation (default is 1)
```
Compiled material.bin files will be at `build/<platform>/`

## Note

**Shaders are not officially supported on MCBE**.
