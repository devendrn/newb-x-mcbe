import os
import shutil
import json
import tomllib
import subprocess
import platform
from importlib import import_module
from rich.console import Console
from rich.status import Status
from util import print_styled_error, get_materials_path, create_pack_manifest
from lazurite.compiler.macro_define import MacroDefine

console = Console()
status = console.status("[bold]")


SHADERC_PATH = os.path.join('tool', 'data', 'shaderc')
if platform == 'nt':
    SHADERC_PATH += ".exe"

_current_subpack = "default"
_last_log = ""


def _lp_print_override(m):
    global _last_log
    if not _last_log == "":
        status.console.print(_last_log)
    log = f"[bold dim]\\[{_current_subpack}][/] {m}"
    status.update(log)
    _last_log = "  " + log + " - [green]success"


# monkey patch print
lp = import_module('lazurite.project.project')
lp.print = _lp_print_override


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
    lp.print = _lp_print_override

    with open('src/newb/pack_config.toml', 'rb') as f:
        pack_config = tomllib.load(f)

    if args.use_git:
        # tag-commits_count-last_commit_id
        # eg: v15-39-g336ac45
        res = subprocess.run(['git', 'describe', '--tags'], capture_output=True)
        commit = res.stdout.decode('utf-8').split('-')
        tag = commit[0][1:]
        commits = commit[1]
        pack_config['version'] = [0, int(tag), int(commits)]

    pack_name: str = pack_config['name']
    pack_version = f"{pack_config['version'][1]}.{pack_config['version'][2]}"
    profile: str = args.p

    console.print(" [bold green]Newb Pack Builder[/] \n [dim]build tool: Lazurite\n", style="")
    console.print("~ Pack info", style="bold")
    console.print("  [dim]name    :", "[cyan]" + pack_name)
    console.print("  [dim]authors :", "[cyan]" + ', '.join(pack_config['authors']))
    console.print("  [dim]version :", "[cyan]" + pack_version + "\n")

    console.print("~ Build target", style="bold")
    console.print("  [dim]profile :", "[cyan]" + args.p)

    pack_acr_name = "".join(filter(str.isupper, pack_name)).lower()
    pack_acr_name = f"{pack_acr_name}-{pack_version}-{profile}"
    pack_dir = os.path.join('build', pack_acr_name)
    mats_dir = os.path.join(pack_dir, 'renderer', 'materials')

    shutil.copytree('assets', pack_dir, dirs_exist_ok=True)

    pack_description: str = pack_config['description']['long']

    patch_warning = "Only works with "
    if profile == 'android':
        patch_warning += "Patched Minecraft"
    elif profile == 'windows':
        patch_warning += " BetterRenderDragon"
    elif profile == 'merged':
        patch_warning += " BetterRenderDragon or Patched Minecraft"
    else:  # ios
        patch_warning = "Materials need to be installed manually for shader to work"

    pack_description = pack_description.replace("%w", patch_warning).replace("%v", "v" + pack_version)
    pack_config['description']['long'] = pack_description
    pack_manifest = create_pack_manifest(pack_config)

    console.print("\n~ Build materials", style="bold")

    status.start()

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

    if not args.no_zip:
        pack_archive = pack_dir + ('.zip' if is_ios else '.mcpack')
        console.print("\n~ [bold]Archive pack\n ", pack_archive)
        shutil.make_archive(pack_dir, 'zip', pack_dir)
        if not is_ios:
            os.rename(pack_dir + '.zip', pack_archive)
