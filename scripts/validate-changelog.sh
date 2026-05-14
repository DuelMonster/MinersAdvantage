#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
changelog_path="$repo_root/CHANGELOG.md"
mapfile -t staged_files < <(git -C "$repo_root" diff --cached --name-only --diff-filter=ACMR)

if [[ ${#staged_files[@]} -eq 0 ]]; then
  echo "CHANGELOG validation passed."
  exit 0
fi

changelog_staged=false
requires_changelog_update=false

for file in "${staged_files[@]}"; do
  normalized="${file//\\//}"
  if [[ "$normalized" == "CHANGELOG.md" ]]; then
    changelog_staged=true
    continue
  fi

  if [[ "$normalized" == ".brainbox/state/version-bump-state.txt" ]]; then
    continue
  fi

  requires_changelog_update=true
done

if [[ "$requires_changelog_update" == true && "$changelog_staged" == false ]]; then
  echo "CHANGELOG update required: stage CHANGELOG.md when committing substantive changes." >&2
  exit 1
fi

if [[ ! -f "$changelog_path" ]]; then
  echo "CHANGELOG.md is missing." >&2
  exit 1
fi

# Check that CHANGELOG has at least one version header (e.g., ## 0.18.0)
if ! grep -E "^## [0-9]+\.[0-9]+\.[0-9]+" "$changelog_path" > /dev/null 2>&1; then
  echo "CHANGELOG.md missing version headers (format: ## X.Y.Z)" >&2
  exit 1
fi

# Check that there is at least one bullet entry anywhere in the file
if ! grep -E "^-" "$changelog_path" > /dev/null 2>&1; then
  echo "CHANGELOG.md has version headers but no entries (format: - description)" >&2
  exit 1
fi

echo "CHANGELOG validation passed."
exit 0
