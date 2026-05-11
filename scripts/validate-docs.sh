#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readme_path="$repo_root/README.md"
technical_path="$repo_root/TECHNICAL.md"

if [[ ! -f "$readme_path" ]]; then
  echo "README.md is missing."
  exit 1
fi

if [[ ! -f "$technical_path" ]]; then
  echo "TECHNICAL.md is missing."
  exit 1
fi

readme="$(cat "$readme_path")"
technical="$(cat "$technical_path")"

missing=()

required_readme_sections=(
  "## Overview"
  "## Features"
  "## How It Works"
  "## Supported Crops"
  "## Installation"
  "## Configuration"
  "## Compatibility"
  "## Technical Documentation"
  "## License"
  "## Credits"
)

required_technical_sections=(
  "## Build Instructions"
  "## License"
  "## Credits"
)

for section in "${required_readme_sections[@]}"; do
  if ! grep -Fq "$section" <<< "$readme"; then
    missing+=("README missing required section: $section")
  fi
done

for section in "${required_technical_sections[@]}"; do
  if ! grep -Fq "$section" <<< "$technical"; then
    missing+=("TECHNICAL missing required section: $section")
  fi
done

if ! grep -Eq '\[TECHNICAL\.md\]\(TECHNICAL\.md\)' <<< "$readme"; then
  missing+=("README missing link to TECHNICAL.md in markdown link format.")
fi

if ! grep -Eiq '^MIT\b' <<< "$readme"; then
  missing+=("README must explicitly state MIT license.")
fi

if ! grep -Eiq '^MIT\b' <<< "$technical"; then
  missing+=("TECHNICAL must explicitly state MIT license.")
fi

# Installation section must cover both loaders
if grep -Fq '## Installation' <<< "$readme"; then
  if ! grep -Eiq '\bFabric\b' <<< "$readme"; then
    missing+=("README Installation section must mention Fabric.")
  fi
  if ! grep -Eiq '\bNeoForge\b' <<< "$readme"; then
    missing+=("README Installation section must mention NeoForge.")
  fi
fi

if (( ${#missing[@]} > 0 )); then
  echo
  echo "Documentation validation failed:"
  for issue in "${missing[@]}"; do
    echo "- $issue"
  done
  echo
  echo "Fix documentation to satisfy .brainbox/guides/DOCUMENTATION_STANDARDS.md before committing."
  exit 1
fi

echo "Documentation validation passed."
exit 0
