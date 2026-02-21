import os
from rich.console import Console
import pickle
import platform

CONF_FILE = "tool/data/.builder.pkl"
NS_DEV_RELEASE = "https://github.com/devendrn/newb-shader/releases/download/dev/"
NS_DEV_MAT_SRC_URL = NS_DEV_RELEASE + "src-materials-1.26.0.zip"
NS_DEV_SHADERC_URL_PREFIX = NS_DEV_RELEASE + "shaderc-"
SHADERC_PATH = os.path.join('tool', 'data', 'shaderc')
if platform == 'nt':
    SHADERC_PATH += ".exe"
SRC_MATERIALS_EG_PATH = os.path.join('tool', 'data', 'materials', 'Sky.material.json')


def print_styled_error(console: Console, log: str):
    log = log.strip().split('\n')
    for line in log[:-2]:
        split_line = line.split()
        style = 'dim'
        if not split_line:
            print('')
            continue
        if split_line[0] == "Command:":
            style = 'dim'
        elif split_line[0] == "Warning:":
            style = 'red'
        elif split_line[0] in ["Error:", ">>>", "cpp:"]:
            style = 'bold red'

        console.print(line, style=style)


def get_materials_path(mats: [str]):
    materials_path = os.path.join('src', 'materials')
    all_materials = os.listdir(materials_path)
    material_patterns = []
    for m in mats:
        if m not in all_materials:
            raise FileNotFoundError(m)
        material_patterns.append(os.path.join(materials_path, m))
    return material_patterns


def create_pack_manifest(config: dict) -> dict:
    return {
        'format_version': 2,
        'header': {
            "name": config['name'],
            "description": config['description'],
            "uuid": config['uuid'],
            "version": config['version'],
            "min_engine_version": config['min_supported_mc_version']
        },
        'modules': [
            {
                'type': 'resources',
                'uuid': '900f3d8b-37b4-465f-8f56-941687e36c35',
                'version': config['version']
            }
        ],
        'subpacks': [],
        'metadata': {
            'authors': config['authors'],
            'url': config['url']
        }
    }


def load_conf():
    try:
        with open(CONF_FILE, 'rb') as file:
            conf = pickle.load(file)
            if type(conf) is dict:
                return conf
        return {}
    except Exception:
        return {}


def save_conf(conf):
    with open(CONF_FILE, 'wb') as file:
        pickle.dump(conf, file)


def check_conf(console: Console):
    # returns None if setup needs update
    conf = load_conf()
    is_diff_machine = conf.get("os_name") != platform.system() or conf.get("arch") != platform.machine()
    is_diff_shaderc_url_prefix = conf.get("shaderc_url_prefix") != NS_DEV_SHADERC_URL_PREFIX
    is_diff_mat_src_url = conf.get("mat_src_url") != NS_DEV_MAT_SRC_URL
    is_dependencies_present = os.path.exists(SHADERC_PATH) and os.path.exists(SRC_MATERIALS_EG_PATH)

    if is_diff_shaderc_url_prefix or is_diff_machine:
        console.print("Error: Incompatible or outdated shaderc binary.", style="red")
        conf = None
    if is_diff_mat_src_url:
        console.print("Error: Outdated material source files.", style="red")
        conf = None
    if not is_dependencies_present:
        console.print("Error: 'setup' not done.", style="red")
        conf = None

    if conf is None:
        console.print("       Please run build setup again.", style="bold red")

    return conf



