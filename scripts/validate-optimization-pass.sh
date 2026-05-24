#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

mapfile -t staged < <(git diff --cached --name-only --diff-filter=ACMR)
if (( ${#staged[@]} == 0 )); then
  echo "Optimization validation skipped: no staged files."
  exit 0
fi

errors=()

is_binary_file() {
  local file="$1"
  LC_ALL=C grep -Iq . "$file"
  local status=$?
  if [[ $status -eq 0 ]]; then
    return 1
  fi
  if [[ $status -eq 1 ]]; then
    return 0
  fi
  return 1
}

for relative in "${staged[@]}"; do
  [[ -z "$relative" ]] && continue
  full_path="$repo_root/$relative"
  [[ -f "$full_path" ]] || continue

  if is_binary_file "$full_path"; then
    continue
  fi

  if [[ "$relative" =~ \.java$ ]]; then
    mapfile -t imports < <(grep -E '^\s*import\s+.+;\s*$' "$full_path" | sed 's/^\s*//;s/\s*$//')
    if (( ${#imports[@]} > 0 )); then
      dupes=$(printf '%s\n' "${imports[@]}" | sort | uniq -d || true)
      if [[ -n "$dupes" ]]; then
        while IFS= read -r dup; do
          [[ -z "$dup" ]] && continue
          errors+=("$relative has duplicate import: $dup")
        done <<< "$dupes"
      fi

      for imp in "${imports[@]}"; do
        if [[ "$imp" =~ ^import[[:space:]]+.+\.\*\;[[:space:]]*$ ]]; then
          errors+=("$relative uses wildcard import: $imp")
        fi
      done
    fi

    if grep -nE 'TODO|FIXME|XXX' "$full_path" >/dev/null; then
      while IFS= read -r row; do
        errors+=("$relative contains TODO/FIXME/XXX marker at line $row")
      done < <(grep -nE 'TODO|FIXME|XXX' "$full_path" | cut -d: -f1)
    fi

    if grep -nE '[[:space:]]+$' "$full_path" >/dev/null; then
      while IFS= read -r row; do
        errors+=("$relative has trailing whitespace at line $row")
      done < <(grep -nE '[[:space:]]+$' "$full_path" | cut -d: -f1)
    fi

    if grep -nE 'catch\s*\(\s*Throwable\s+ignored\s*\)' "$full_path" >/dev/null; then
      while IFS= read -r row; do
        errors+=("$relative uses catch(Throwable ignored) at line $row; use util.ExceptionHandler instead")
      done < <(grep -nE 'catch\s*\(\s*Throwable\s+ignored\s*\)' "$full_path" | cut -d: -f1)
    fi
  fi

  if [[ "$relative" =~ ^src/main/ || "$relative" =~ ^src/test/ ]]; then
    if grep -nE '[[:space:]]+$' "$full_path" >/dev/null; then
      while IFS= read -r row; do
        errors+=("$relative has trailing whitespace at line $row")
      done < <(grep -nE '[[:space:]]+$' "$full_path" | cut -d: -f1)
    fi
  fi
done

if (( ${#errors[@]} > 0 )); then
  echo
  echo "Optimization validation failed:"
  for e in "${errors[@]}"; do
    echo "- $e"
  done
  echo
  echo "Fix staged issues before committing. Rule source: .brainbox/rules/todo.optimisation.pass.md"
  exit 1
fi

echo "Optimization validation passed."
exit 0
