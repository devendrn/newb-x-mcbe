import os
import zipfile
import signal
import platform
import shutil
from functools import partial
from threading import Event
from urllib.request import urlopen
from rich.progress import (
    BarColumn,
    DownloadColumn,
    Progress,
    TextColumn,
    TransferSpeedColumn,
)

done_event = Event()


def handle_sigint(signum, frame):
    done_event.set()


signal.signal(signal.SIGINT, handle_sigint)


progress = Progress(
    TextColumn("[bold blue]{task.fields[filename]}", justify="right"),
    BarColumn(bar_width=None),
    "•",
    DownloadColumn(),
    "•",
    TransferSpeedColumn(),
)


def _download_file(url: str, path: str) -> None:
    filename = url.split("/")[-1]
    task_id = progress.add_task("download", filename=filename, start=False)
    response = urlopen(url)
    progress.update(task_id, total=int(response.info()["Content-length"]))
    with open(path, "wb") as dest_file:
        progress.start_task(task_id)
        for data in iter(partial(response.read, 32768), b""):
            dest_file.write(data)
            progress.update(task_id, advance=len(data))
            if done_event.is_set():
                return
    progress.console.print(f"Downloaded to {path}")


NS_DEV_RELEASE = "https://github.com/devendrn/newb-shader/releases/download/dev/"


def run(args):
    arch = platform.machine()

    data_path = os.path.join('tool', 'data')
    mat_path = os.path.join(data_path, 'materials')

    shaderc_url = NS_DEV_RELEASE + "shaderc"
    shaderc_path = os.path.join(data_path, "shaderc")
    if os.name == 'nt':
        shaderc_url += ".exe"
        shaderc_path += ".exe"
    else:
        if arch == 'x86_64':
            shaderc_url += ".x86_64"
        elif arch in ['aarch64']:
            shaderc_url += ".arm64"
        elif arch in ['armv8l', 'armv8l']:
            shaderc_url += ".arm32"
        else:
            progress.console.print("No shaderc version found for", arch, style='red')
            exit(1)

        # libc++_shared.so not found fix for termux
        lib_path = "tool/lib"
        if not os.path.exists(lib_path):
            os.mkdir(lib_path)

        termux_lib_file = "/data/data/com.termux/files/usr/lib/libc++_shared.so"
        if os.path.exists(termux_lib_file) and not os.path.exists(lib_path + "/libc++_shared.so"):
            progress.console.print("Adding termux fix for libc++_shared.so not found")
            shutil.copyfile(termux_lib_file, lib_path + "/libc++_shared.so")

    if args.reset:
        shutil.rmtree(data_path)

    if not os.path.exists(data_path):
        os.mkdir(data_path)

    if not os.path.exists(shaderc_path):
        progress.console.print("Downloading shaderc")
        with progress:
            _download_file(shaderc_url, shaderc_path)
        os.chmod(shaderc_path, 0o755)

    test_mat = os.path.join(mat_path, "android", "Sky.material.bin")
    if not os.path.exists(test_mat):
        progress.console.print("Downloading source materials")
        with progress:
            mat_filename = os.path.join(data_path, 'material.zip')
            _download_file(NS_DEV_RELEASE + "src-materials-1.20.80.05.zip", mat_filename)
            with zipfile.ZipFile(mat_filename, 'r') as zip_ref:
                zip_ref.extractall(mat_path)
            os.remove(mat_filename)

    progress.console.print("[bold green]All done!")
