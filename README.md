# Newb X Legacy

**Newb X Legacy** is a RenderDragon successor to the legacy GLSL shader, [Newb Shader](https://github.com/devendrn/newb-shader-mcbe). It is an enhanced vanilla shader that focuses on being lightweight and having soft aesthetics. It supports Minecraft Bedrock 1.21+ (Android/iOS/Windows).

<br>

![Screenshots](docs/screenshots.jpg "Newb X Legacy 15.47, MCBE 1.21.0")

## Downloads
[![GitHub downloads](https://img.shields.io/github/downloads/devendrn/newb-x-mcbe/total?label=GitHub%20downloads)](https://github.com/devendrn/newb-x-mcbe/releases/latest)
[![MCPEDL/Curseforge downloads](https://img.shields.io/curseforge/dt/1179976?label=MCPEDL%2FCurseforge%20downloads&color=eb622b)](https://www.curseforge.com/minecraft-bedrock/texture-packs/newb-shader)


You can download the shader pack from [GitHub releases](https://github.com/devendrn/newb-x-mcbe/releases/latest) or [MCPEDL](https://mcpedl.com/newb-shader/).

## Installation

> [!NOTE]
> Shaders are not officially supported on Minecraft Bedrock. The following are unofficial ways to load shaders. There are multiple ways to get it working. Start with the recommended method. If that doesn't work try the other method.

### Android

| **Using MB Loader app (Recommended):** |
|:-|
| 1. Install [MB Loader app](https://play.google.com/store/apps/details?id=io.bambosan.mbloader). |
| 2. Launch Minecraft from MB Loader app. |
| 3. Import the resource pack and activate it in global resources. |
| [Detailed guide](https://faizul726.github.io/blog/mb-loader/) |

| **Using Patched Minecraft:** |
|:-|
| 1. Install [Patched Minecraft app](https://devendrn.github.io/renderdragon-shaders/shaders/installation/android#using-patch-app). |
| 2. Import the resource pack and activate it in global resources. |


### iOS
There is no easy way to use shaders on Apple devices. You will need to sideload a Minecraft package with shaders using third party tools.  
Use at your own risk. Detailed instructions can be found on YouTube.

### Windows

| **Using BRD Mod (Recommended)** |
|:-|
| 1. Download `BetterRenderDragon.zip` from [QYCottage](https://github.com/QYCottage/BetterRenderDragon/releases/latest). |
| 2. Extract the ZIP file and double click `mcbe_injector`. |
| 2. It will open Minecraft. Import the shader resource pack and activate it in global resources. |

| **Using Matject** |
|:-|
| 1. Follow this [guide](https://faizul726.github.io/matject/docs/guide-for-beginners). |

### Linux / Mac
Following methods are for [mcpelauncher-manifest](https://minecraft-linux.github.io/#installation).

| **Using MBL mod (Recommended): x86_64 arch** |
|:-|
| 1. Download [mcpelauncher-materialbinloader-mod](https://github.com/CrackedMatter/mcpelauncher-materialbinloader). |
| 2. Import the shader resource pack and activate it in global resources. |

| **Using shaders mod: x86_64, x86, arm64, arm arch** |
|:-|
| 1. Download [mcpelauncher-shadersmod](https://github.com/GameParrot/mcpelauncher-shadersmod/releases/latest). |
| 2. Follow this [guide](https://faizul726.github.io/blog/mcpelauncher-mod-installation/) to setup. |

<br>

## Building

### Install dependencies
- [Git](https://git-scm.com/)
- [Python](https://www.python.org/) 3.11 or higher required
- Python packages:
  - [lazurite](https://veka0.github.io/lazurite/#installation) (Must be `v0.6.0`. Newer or older version may not be supported)
  - [rich](https://rich.readthedocs.io/en/stable/introduction.html#installation) (Must be `v13.x.x`)

### Get source code
> [!NOTE]
> Source code contains symlinks. Symlinks may not work as intended on Windows.
> Below command will always ensure that symlinks work as intended.
```
git clone -c core.symlinks=true https://github.com/devendrn/newb-x-mcbe/
cd newb-x-mcbe
```

### Install dependencies
*Skip if you already have installed those versions.*
```
python -m pip install -r requirements.txt
```

### Setup build environment
> [!NOTE]
> - On Windows, run `.\build.bat` instead of `./build.sh` for all following commands.
> - On Android, it's suggested to use [Termux from F-droid](https://f-droid.org/packages/com.termux/) instead of Google Play.
```
./build.sh setup
```
This will download [shaderc binary](https://github.com/devendrn/newb-shader/releases/dev/) and material data required to build shader.

<br>

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

> [!TIP]
> `merged` compiles materials with all platforms combined. So the same compiled material works on supported platforms. This may make build take longer and increase material size.

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
  -v V                  version number eg: 17
```

> [!TIP]
> If you want to customize pack name, author, version and other details, you can do so in `src/newb/pack_config.toml`.

**List of supported materials (for `mats -m M`)**  
- `Actor`
- `ActorGlint`
- `Clouds`
- `EndSky`
- `ItemInHandColor`
- `ItemInHandColorGlint`
- `ItemInHandTextured`
- `LegacyCubemap`
- `RenderChunk`
- `Sky`
- `Stars`
- `SunMoon`
- `Weather`

<br>

## Development

Clangd can be used to get code completion and error checks for source files inside include/newb. Fake bgfx header and clangd config are provided for the same.
- **Neovim**: Install clangd LSP.
- **VSCode**: Install [vscode-clangd](https://marketplace.visualstudio.com/items?itemName=llvm-vs-code-extensions.vscode-clangd) extension.

## License

**Source Code:** The "Newb Shader" source code is licensed under the [MIT License](/LICENSE). You are free to modify, distribute, and create derivative works based on the source code.

**Compiled Resource Packs (`.mcpack` files):** The compiled resource packs distributed by the "Newb Shader" project and its variant creators are copyrighted works with restrictions. See the `COPYRIGHT.txt` file within each resource pack for more information.
