import os
import platform
from rich.console import Console
from importlib import import_module
from lazurite.compiler.macro_define import MacroDefine
from util import print_styled_error, get_materials_path

console = Console()
status = console.status("[bold green]Building...")


def _lp_print_override(m):
    console.print(m, style='bold cyan')
    status.update("[bold green]Building " + m)


# monkey patch print
lp = import_module('lazurite.project.project')
lp.print = _lp_print_override


def run(args):
    output_path = os.path.join('build', args.p)
    shaderc_path = os.path.join('tool', 'data', 'shaderc')
    if platform.os == 'nt':
        shaderc_path += '.exe'

    materials_pattern = []
    if not args.m == "":
        try:
            materials_pattern = get_materials_path(args.m)
        except FileNotFoundError as e:
            console.print(f"Error: '{e.args[0]}' does not exist in project.", style="bold red")
            exit(1)

    if not os.path.exists(output_path):
        os.makedirs(output_path)

    with status:
        try:
            lp.compile(
                os.path.join('src', 'materials'),
                [args.p],
                output_path,
                material_patterns=materials_pattern,
                shaderc_path=shaderc_path,
                defines=[MacroDefine.from_string(args.s)] if args.s else []
            )
        except Exception as e:
            log: str = e.args[0]
            print_styled_error(console, log)
