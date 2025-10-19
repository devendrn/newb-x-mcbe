# LLM generated code. Just for concept.

import os
import shutil
from pathlib import Path

def delete_data_folder():
    data_path = Path(__file__).resolve().parent / "data"

    if not data_path.exists():
        print(f"Folder not found: {data_path}")
        return

    if not data_path.is_dir():
        print(f"Expected a directory but found a file: {data_path}")
        return

    try:
        shutil.rmtree(data_path)
        print(f"Deleted folder: {data_path}")
    except Exception as e:
        print(f"Failed to delete {data_path}: {e}")

if __name__ == "__main__":
    delete_data_folder()
