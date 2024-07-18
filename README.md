# Newb X Legacy

**Newb X Legacy** is a RenderDragon successor to the legacy GLSL shader, [Newb Shader](https://github.com/devendrn/newb-shader-mcbe). It is an enhanced vanilla shader that focuses on being lightweight and having soft aesthetics. It supports Minecraft Bedrock 1.21+ (Windows/Android/iOS).

> [!WARNING]
> This is an experimental repository, breaking changes are made often.

![Screenshots](docs/screenshots.jpg "Newb X Legacy 15.47, MCBE 1.21.0")

<br>

## Downloads

Nightly builds for Android (ESSL), Windows (DX), and iOS (Metal) can be found at the [Discord server](https://discord.gg/newb-community-844591537430069279).

You can also download from [v15-dev releases](https://github.com/devendrn/newb-x-mcbe/releases/tag/v15-dev)

<br>

## Installation

> [!NOTE]
> Shaders are not officially supported on Minecraft Bedrock. The following are unofficial ways to load shaders.

**Linux / Mac:** For [mcpelauncher-manifest](https://mcpelauncher.readthedocs.io/en/latest/getting_started/index.html) only
1. Download [mcpelauncher-shadersmod](https://github.com/GameParrot/mcpelauncher-shadersmod/releases/latest).
2. Follow this [guide](https://faizul118.github.io/guides/shadersmodinstallation) to setup.

**Windows:**
1. Use [BetterRenderDragon](https://github.com/ddf8196/BetterRenderDragon) to enable MaterialBinLoader.
2. Import the resource pack and activate it in global resources.

**Android:**
1. Install [Patched Minecraft App](https://devendrn.github.io/renderdragon-shaders/shaders/installation/android#using-patch-app)
2. Import the resource pack and activate it in global resources.

<br>

## Building

### Install dependencies
- [Git](https://git-scm.com/)
- [Python](https://www.python.org/) 3.11 or higher required
- Python packages:
  - [lazurite](https://veka0.github.io/lazurite/#installation) (must be v0.2.1. newer or older version may not be supported)
  - [rich](https://rich.readthedocs.io/en/stable/introduction.html#installation) (must be v13.x.x)

### Get source code
```
git clone https://github.com/devendrn/newb-x-mcbe/
cd newb-x-mcbe
```

### Setup build environment
> [!NOTE]
> On Windows, you should use `.\build.bat` instead of `./build.sh` for all following commands. 
```
./build.sh setup
```
This will download shaderc binary and material data required to build shader.

### Compile specific shader materials
```
./build.sh mats
```  
Compiled material.bin files will be inside `build/<platform>/`  
**Command usage:**
```
usage: build mats [-h] [-p {android,windows,merged,ios}] [-m M [M ...]] [-s S]

options:
  -h, --help            show this help message and exit
  -p {android,windows,merged,ios}
                        build profile
  -m M [M ...]          build materials (eg: Sky)
  -s S                  subpack config to use (eg: NO_WAVE)
```

### Compile and build full shader pack
```
./build.sh pack
```
The final mcpack will be inside `build/`.  
**Command usage:**
```
usage: build pack [-h] [-p {android,windows,merged,ios}] [--no-zip] [--no-label] [-v V]

options:
  -h, --help            show this help message and exit
  -p {android,windows,merged,ios}
                        build profile
  --no-zip              don't make archive
  --no-label            don't label materials
  -v V                  version number eg: 46
```

<br>

## Development

Clangd can be used to get code completion and error checks for source files inside include/newb. Fake bgfx header and clangd config are provided for the same.
- **Neovim** (NvChad): Install clangd LSP from Mason.
- **VSCode**: Install [vscode-clangd](https://marketplace.visualstudio.com/items?itemName=llvm-vs-code-extensions.vscode-clangd) extension.
