#!/usr/bin/env bash
set -euo pipefail
project_root=${1:-$(cd -- "$(dirname -- "$0")/.." && pwd)}
cd "$project_root"

git diff --exit-code HEAD -- data/schemas/
untracked=$(git ls-files --others --exclude-standard -- data/schemas/)
if [ -n "$untracked" ]; then
    printf 'Generated Room schemas are missing from Git:\n%s\n' "$untracked" >&2
    exit 1
fi
echo "Generated Room schemas match the committed exports."
