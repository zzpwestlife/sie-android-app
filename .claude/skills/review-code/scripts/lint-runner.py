#!/usr/bin/env python3
from __future__ import annotations

import shutil
import subprocess
from pathlib import Path


def has_file(name: str) -> bool:
    return Path(name).exists()


def run(cmd: list[str]) -> int:
    # Use shell=True for npm commands on some systems, but list is safer generally.
    # On Windows npm is a cmd script, on Unix it's a bin.
    # We'll rely on subprocess finding it in PATH.
    try:
        result = subprocess.run(cmd, check=False)
        return result.returncode
    except FileNotFoundError:
        return 127


def main() -> int:
    if has_file("package.json"):
        if shutil.which("npm") is None and shutil.which("yarn") is None:
            return 2
        
        # Prefer yarn if lockfile exists, otherwise npm
        if has_file("yarn.lock"):
            return run(["yarn", "lint"])
        else:
            return run(["npm", "run", "lint"])

    # Fallback or other languages
    if has_file("requirements.txt") or has_file("pyproject.toml"):
        if shutil.which("flake8") is None:
            return 3
        return run(["flake8", "."])
        
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
