import os
import shutil
import json
import tomllib
import subprocess
import platform
from rich.console import Console
from rich.status import Status

console = Console()

SHADERC_PATH = os.path.join('tool', 'data', 'shaderc')
if platform == 'nt':
    SHADERC_PATH += ".exe"

env = os.environ.copy()
if os.name == "posix":
    env.update(LD_LIBRARY_PATH="./tool/lib")


def _build_mat(status: Status, profile: str, subpack: str, material: str, output_path: str):
    global errors

    log = f"[bold dim]\\[{subpack}][/bold dim] {material}"
    status.update(log)

    cmd = [
        'lazurite', 'build', 'src/materials',
        '-p', profile,
        '-o', output_path,
        '-m', material,
        '--shaderc', SHADERC_PATH,
        '-d', subpack.upper()
    ]

    res = subprocess.run(cmd, capture_output=True, env=env)

    res_str = res.stdout.decode('utf-8')
    split_res = res_str.split('\n')

    if res.returncode == 0:
        console.print(" ", log, "- [bold green]success")
        return
    else:
        console.print(" ", log, "- [bold red]fail")

    for i in split_res[3:]:
        if i == "Failed to build shader.":
            continue

        style = "dim"
        if i.startswith(">>>"):
            style = "bold yellow"
        elif "Error: " in i:
            style = "bold red"

        console.print(" ", i, style=style)

    console.print(res.stderr.decode('utf-8'))
    status.stop()
    exit(1)


def run(args):
    with open('src/newb/pack_config.toml', 'rb') as f:
        pack_config = tomllib.load(f)

    pack_name = pack_config['name']
    pack_version = f"{pack_config['version'][1]}.{pack_config['version'][2]}"
    profile: str = args.p

    console.print(" Newb Pack Builder", style="bold green")
    console.print(" build tool: Lazurite", style="dim")
    console.print("\n~ Pack info", style="bold")
    console.print("  [dim]name    :", "[cyan]" + pack_name)
    console.print("  [dim]authors :", "[cyan]" + ', '.join(pack_config['authors']))
    console.print("  [dim]version :", "[cyan]" + pack_version)

    console.print("\n~ Build target", style="bold")
    console.print("  [dim]profile :", "[cyan]" + args.p)

    pack_acr_name = "".join(filter(str.isupper, pack_name)).lower()
    pack_acr_name = f"{pack_acr_name}-{pack_version}-{profile}"
    pack_dir = os.path.join('build', pack_acr_name)
    mats_dir = os.path.join(pack_dir, 'renderer', 'materials')

    if not os.path.exists(mats_dir):
        os.makedirs(mats_dir)

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
        patch_warning = "Material need to be installed manually. This pack only includes texture assets."

    pack_description = pack_description.replace("%w", patch_warning).replace("%v", "v" + pack_version)
    pack_config['description']['long'] = pack_description
    pack_manifest = _create_pack_manifest(pack_config)

    console.print("\n~ Build materials", style="bold")

    status = console.status("[bold blue]")
    status.start()

    for m in pack_config['materials']:
        _build_mat(status, args.p, "default", m, os.path.join(pack_dir, 'renderer', 'materials'))

    for subpack in pack_config['subpack']:
        subpack_name: str = subpack['define'].lower()
        subpack_mats_path = os.path.join(pack_dir, 'subpacks', subpack_name, 'renderer', 'materials')
        if not os.path.exists(subpack_mats_path):
            os.makedirs(subpack_mats_path)
        for m in subpack['materials']:
            mat_path = os.path.join(subpack_mats_path)
            _build_mat(status, args.p, subpack_name, m, mat_path)

        pack_manifest['subpacks'].append(
            {
                'folder_name': subpack_name,
                'name': subpack['description'].rstrip(),
                'memory_tier': 1
            }
        )

    status.stop()

    with open(os.path.join(pack_dir, 'manifest.json'), 'w') as f:
        json.dump(pack_manifest, f, indent=2)

    if not args.no_zip:
        console.print("\n~ [bold]Archive pack\n ", pack_dir + '.mcpack')
        shutil.make_archive(pack_dir, 'zip', pack_dir)
        os.rename(pack_dir + '.zip', pack_dir + '.mcpack')


def _create_pack_manifest(config: dict) -> dict:
    return {
        'format_version': 2,
        'header': {
            "name": config['name'],
            "description": config['description']['long'],
            "uuid": config['uuid']['header'],
            "version": config['version'],
            "min_engine_version": config['min_supported_mc_version']
        },
        'modules': [
            {
                'description': config['description']['short'],
                'type': 'resources',
                'uuid': config['uuid']['module'],
                'version': config['version']
            }
        ],
        'subpacks': [],
        'metadata': {
            'authors': config['authors'],
            'url': config['url']
        }
    }
