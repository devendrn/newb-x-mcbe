# Newb X Legacy

**Newb X Legacy** is a RenderDragon successor to the legacy GLSL shader, [Newb Shader](https://github.com/devendrn/newb-shader-mcbe). It is an enhanced vanilla shader that focuses on being lightweight and having soft aesthetics. It supports Minecraft Bedrock 1.20 (Windows/Android/*iOS).

> [!WARNING]
> This is an experimental repository, breaking changes are made often.

![Screenshot1](docs/screenshots.jpg "Newb X Legacy 15b2, MCBE 1.20.12")

## Downloads

Nightly builds for Android (ESSL) and Windows (DX) can be found at the [Discord server](https://discord.gg/newb-community-844591537430069279).

## Installation

> [!NOTE]
> Shaders are not officially supported on Minecraft Bedrock. The following are unofficial ways to load shaders.

**Linux:** [mcpelauncher-manifest](https://github.com/minecraft-linux/mcpelauncher-ui-manifest)
1. Extract material.bin files from shader mcpack / build materials manually
2. Move these files to data root `mcpelauncher/versions/1.20.x/assets/renderer/materials/`. (Make sure to backup all files in this folder first)
3. Import the resource pack and activate it in global resources.

**Windows:**
1. Use [BetterRenderDragon](https://github.com/ddf8196/BetterRenderDragon) to enable MaterialBinLoader.
2. Import the resource pack and activate it in global resources.

**Android:**
1. Install [Patched Minecraft App](https://devendrn.github.io/renderdragon-shaders/shaders/installation/android#using-patch-app)
2. Import the resource pack and activate it in global resources.

## Building

**Windows:**
1. Setup build environment: `.\setup.bat`
2. Compile material src files: `.\build.bat`

**Linux:**
1. Setup build environment: `./setup.sh`
2. Compile material src files: `./build.sh`  

**Available parameters for the build script:**
| Option | Parameter description |
| :-: | :- |
| -p | Target platforms (Android, Windows, iOS, Merged) |
| -m | Materials to compile (if unspecified, builds all material files) |
| -t | Number of threads to use for compilation (default is CPU core count) |

For example, to build only terrain for Android and Windows, use:
```
.\build.bat -p Windows Android -m RenderChunk
```
Compiled material.bin files will be inside `build/<platform>/`

### Pack
To build the final pack, including all subpacks, use `pack.sh`. If you are on Windows, use a bash shell like Git Bash to run this script file. (Make sure to use the -w tag when you are running the script from a Windows machine) 

**Linux:**
```
./pack.sh -v 15.0
```
**Windows:**
```
./pack.sh -w -v 15.0 -p Windows
./pack.sh -w -v 15.0 -p Android
```
The final pack files will be inside `build/<platform>/temp/`. 

## Development

Clangd can be used to get code completion and error checks for source files inside include/newb. Fake bgfx header and clangd config are provided for the same.
- **Neovim** (NvChad): Install clangd LSP from Mason.
- **VSCode**: Install [vscode-clangd](https://marketplace.visualstudio.com/items?itemName=llvm-vs-code-extensions.vscode-clangd) extension.

