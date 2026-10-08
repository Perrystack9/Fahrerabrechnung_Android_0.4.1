#!/usr/bin/env python3
"""Only skip Android verification for an explicit, conservative documentation allowlist."""

import argparse
from pathlib import PurePosixPath
import re
import subprocess


def documentation_only(path: str) -> bool:
    file = PurePosixPath(path)
    if len(file.parts) == 1:
        return file.suffix.lower() == ".md"
    if file.parts[0] == "docs":
        return file.suffix.lower() in {".md", ".png", ".jpg", ".jpeg", ".webp", ".svg", ".pdf"}
    return path == "ci/README.md"


def android_required(base: str, head: str) -> bool:
    # Dispatches, initial pushes and unavailable comparison bases must run everything.
    if not re.fullmatch(r"[0-9a-f]{40}", head):
        raise ValueError("head must be a full commit SHA")
    if not re.fullmatch(r"[0-9a-f]{40}", base) or base == "0" * 40:
        return True
    try:
        output = subprocess.check_output(
            ["git", "diff", "--no-renames", "--name-only", "-z", base, head, "--"],
            stderr=subprocess.PIPE,
        )
    except subprocess.CalledProcessError:
        return True
    paths = output.decode("utf-8", errors="surrogateescape").split("\0")
    paths = [path for path in paths if path]
    # Include both sides of a rename, and run everything for an empty/ambiguous diff.
    return not paths or any(not documentation_only(path) for path in paths)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", default="")
    parser.add_argument("--head", required=True)
    args = parser.parse_args()
    required = android_required(args.base, args.head)
    print("android=" + str(required).lower())
