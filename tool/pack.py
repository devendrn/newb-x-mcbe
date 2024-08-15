import os
import shutil
import json
import tomllib
import platform
from importlib import import_module
from rich.console import Console
from rich.status import Status
from util import print_styled_error, get_materials_path, create_pack_manifest
from lazurite.compiler.macro_define import MacroDefine
from lazurite import util

console = Console()
status = console.status("[bold]")


SHADERC_PATH = os.path.join('tool', 'data', 'shaderc')
if platform == 'nt':
    SHADERC_PATH += ".exe"

_current_subpack = "default"
_last_log = ""
_name = ""


def _lp_print_override(m):
    global _last_log
    if not _last_log == "":
        status.console.print(_last_log)
    log = f"[bold dim]\\[{_current_subpack}][/] {m}"
    status.update(log)
    _last_log = "  " + log + " - [green]success"


# monkey patch print and label
lp = import_module('lazurite.project.project')
lp.print = _lp_print_override


def mlabel(
    self,
    material_name: str,
    pass_name: str,
    variant_index: int,
    is_supported: bool,
    flags: dict,
):
    global _name, _current_subpack

    if not any(
        self.platform.name.startswith(platform_prefix)
        for platform_prefix in ["ESSL", "GLSL", "Metal"]
    ):
        return self

    comment = (f"// {_name} ({_current_subpack})")
    code = self.bgfx_shader.shader_bytes.decode()
    code = util.insert_header_comment(code, comment)
    self.bgfx_shader.shader_bytes = code.encode()

    return self


lp.ShaderDefinition.label = mlabel
owrite = lp.Material.write


def mwrite(self, file):
    self.passes[0].label(self.name)
    owrite(self, file)


def _exit_with_error():
    status.stop()
    exit(1)


def _build(status: Status, profile: str, subpack: str, materials: [str], output_path: str):
    global _current_subpack, _last_log

    _current_subpack = subpack
    _last_log = ""

    try:
        material_patterns = get_materials_path(materials)
    except FileNotFoundError as e:
        console.print(f"Error: Material '{e.args[0]}' does not exist in project.", style="bold red")
        _exit_with_error()

    subpack_define = MacroDefine.from_string(subpack.upper())

    if not os.path.exists(output_path):
        os.makedirs(output_path)

    try:
        lp.compile(
            os.path.join('src', 'materials'),
            [profile],
            output_path,
            material_patterns=material_patterns,
            shaderc_path=SHADERC_PATH,
            defines=[subpack_define]
        )
        status.console.print(_last_log)  # flush last log
    except Exception as e:
        log: str = e.args[0]
        status.console.print(_last_log[:-16] + "- [red]fail")
        print_styled_error(console, log)
        _exit_with_error()


def run(args):
    global _name

    lp.print = _lp_print_override

    console.print(" [bold green]Newb Pack Builder[/] \n [dim]build tool: Lazurite\n", style="")

    with open('src/newb/pack_config.toml', 'rb') as f:
        pack_config = tomllib.load(f)

    if args.v:
        if args.v.isdigit():
            pack_config['version'][2] = int(args.v)
        else:
            console.print("[yellow]Ignoring invalid version number '" + args.v + "' specified using -v")

    pack_name: str = pack_config['name']
    pack_version = f"{pack_config['version'][1]}.{pack_config['version'][2]}"
    pack_authors = ', '.join(pack_config['authors'])
    profile: str = args.p

    console.print("~ Pack info", style="bold")
    console.print("  [dim]name    :", "[cyan]" + pack_name)
    console.print("  [dim]authors :", "[cyan]" + pack_authors)
    console.print("  [dim]version :", "[cyan]" + pack_version + "\n")

    console.print("~ Build target", style="bold")
    console.print("  [dim]profile :", "[cyan]" + args.p)

    pack_acr_name = "".join(filter(str.isupper, pack_name)).lower()
    pack_acr_name = f"{pack_acr_name}-{pack_version}-{profile}"
    pack_dir = os.path.join('build', 'pack-' + args.p)
    mats_dir = os.path.join(pack_dir, 'renderer', 'materials')

    shutil.copytree('assets', pack_dir, dirs_exist_ok=True)

    pack_description: str = pack_config['description']

    patch_warning = "Only works with "
    if profile == 'android':
        patch_warning += "Patched Minecraft"
    elif profile == 'windows':
        patch_warning += "BetterRenderDragon"
    elif profile == 'merged':
        patch_warning += "BetterRenderDragon or Patched Minecraft"
    else:  # ios
        patch_warning = "Materials need to be installed manually for shader to work"

    pack_description = pack_description.replace("%w", patch_warning).replace("%v", "v" + pack_version + "-" + args.p)
    pack_config['description'] = pack_description
    pack_manifest = create_pack_manifest(pack_config)

    console.print("\n~ Build materials", style="bold")

    status.start()

    if not args.no_label:
        _name = pack_name + " v" + pack_version
        lp.Material.write = mwrite

    _build(status, args.p, "default", pack_config['materials'], mats_dir)

    for subpack in pack_config['subpack']:
        subpack_name: str = subpack['define'].lower()
        subpack_path = os.path.join(pack_dir, 'subpacks', subpack_name)
        subpack_mats_path = os.path.join(subpack_path, 'renderer', 'materials')
        mats = subpack['materials']

        if not os.path.exists(subpack_path):
            os.makedirs(subpack_path)

        if mats:
            _build(status, args.p, subpack_name, mats, subpack_mats_path)

        pack_manifest['subpacks'].append(
            {
                'folder_name': subpack_name,
                'name': subpack['description'].rstrip(),
                'memory_tier': 1
            }
        )

    status.stop()

    is_ios = args.p == 'ios'
    if is_ios:
        pack_manifest.pop('subpacks')

    with open(os.path.join(pack_dir, 'manifest.json'), 'w') as f:
        json.dump(pack_manifest, f, indent=2)

    pack_copyright: str = pack_config['info']['copyright']
    pack_copyright = pack_copyright.replace("%a", pack_authors)
    with open(os.path.join(pack_dir, 'COPYRIGHT.txt'), 'w') as f:
        f.write(pack_copyright)

    pack_credits = pack_config['info']['credits']
    if pack_credits:
        with open(os.path.join(pack_dir, 'CREDITS.txt'), 'w') as f:
            f.write(pack_credits)

    if not args.no_zip:
        pack_archive = os.path.join('build', pack_acr_name + ('.zip' if is_ios else '.mcpack'))
        console.print("\n~ [bold]Archive pack\n ", pack_archive)
        shutil.make_archive(pack_dir, 'zip', pack_dir)
        if not is_ios:
            os.rename(pack_dir + '.zip', pack_archive)
