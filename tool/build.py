import subprocess
import os
import platform
from rich.console import Console

console = Console()
status = console.status("[bold green]Building...")


def _print_styled(line: str):
    line = line.rstrip()
    split_line = line.split()

    style = 'dim'
    if len(split_line) <= 1:
        style = 'bold cyan'
        status.update("[bold green]Building " + line)
    elif split_line[0] == "Completed":
        status.stop()
        style = 'dim'
    elif split_line[0] == "Warning:":
        style = 'red'
    elif split_line[0] in ["Error:", ">>>", "cpp:"]:
        style = 'bold red'
    elif split_line[0] == "Compiling":
        return

    console.print(line, style=style)


def run(args):
    shaderc_path = os.path.join('tool', 'data', 'shaderc')
    if platform.os == 'nt':
        shaderc_path += '.exe'

    output_path = os.path.join('build', args.p)
    cmd = [
        'python3', '-u', '-m', 'lazurite', 'build', 'src/materials',
        '-p', args.p,
        '-o', output_path,
        '--shaderc', shaderc_path
    ]

    if args.s:
        cmd = cmd + ['-d', args.s]

    if not args.m == "":
        cmd.append('-m')
        all_materials = os.listdir(os.path.join('src', 'materials'))
        for m in args.m:
            if m not in all_materials:
                console.print("Error: '" + m + "' does not exist in project.", style="bold red")
                exit(1)
        cmd = cmd + args.m

    env = os.environ.copy()
    if os.name == "posix":
        env.update(LD_LIBRARY_PATH="./tool/lib")

    if not os.path.exists('build'):
        os.mkdir('build')

    if not os.path.exists(output_path):
        os.mkdir(output_path)

    status.start()
    with subprocess.Popen(cmd, stdout=subprocess.PIPE, bufsize=1, universal_newlines=True, env=env) as process:
        for line in process.stdout:
            _print_styled(line)

    exit(process.returncode)
