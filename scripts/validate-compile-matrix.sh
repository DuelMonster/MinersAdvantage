#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root"

# Runs before the skip switch: node folders own only gradle.properties, everything else there is generated.
stray_node_sources=()
for node_dir in "$repo_root"/versions/*/; do
  [[ -d "${node_dir}src" ]] && stray_node_sources+=("versions/$(basename "$node_dir")/src")
done

if [[ ${#stray_node_sources[@]} -gt 0 ]]; then
  echo "Found source directories inside Stonecutter node folders:"
  for stray_path in "${stray_node_sources[@]}"; do
    echo "- $stray_path"
  done
  echo
  echo "Sources there bypass Stonecutter preprocessing and drift between nodes."
  echo "Move them into src/main/java behind Stonecutter conditions (see TECHNICAL.md, Source Layout)."
  exit 1
fi

if [[ "${MA_SKIP_COMPILE_MATRIX:-0}" == "1" ]]; then
  echo "Compile matrix validation skipped via MA_SKIP_COMPILE_MATRIX=1."
  exit 0
fi

gradle_wrapper="$repo_root/gradlew"
if [[ ! -f "$gradle_wrapper" ]]; then
  gradle_wrapper="$repo_root/gradlew.bat"
fi
if [[ ! -f "$gradle_wrapper" ]]; then
  echo "Missing Gradle wrapper (gradlew/gradlew.bat)."
  exit 1
fi

versions_root="$repo_root/versions"
if [[ ! -d "$versions_root" ]]; then
  echo "Missing versions directory; cannot run compile matrix validation."
  exit 1
fi

mapfile -t nodes < <(find "$versions_root" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' \
  | grep -E '^[0-9]+(\.[0-9]+){1,3}-(fabric|neoforge)$' \
  | while read -r node; do
      if [[ -f "$versions_root/$node/gradle.properties" ]]; then
        echo "$node"
      fi
    done \
  | sort)

if [[ ${#nodes[@]} -eq 0 ]]; then
  echo "No Stonecutter nodes found under versions/ (expected <mcVersion>-<loader> folders)."
  exit 1
fi

tasks=()
for node in "${nodes[@]}"; do
  tasks+=(":${node}:compileJava")
  tasks+=(":${node}:compileTestJava")
done

echo "Compile matrix validation will check ${#nodes[@]} nodes:"
for node in "${nodes[@]}"; do
  echo "- $node"
done

run_pass() {
  local label="$1"
  shift
  echo
  echo "[$label] Running compile tasks..."
  "$gradle_wrapper" --no-daemon --console=plain "$@" "${tasks[@]}"
}

if run_pass "Pass 1"; then
  echo
  echo "Compile matrix validation passed."
  exit 0
fi

echo
echo "Compile matrix failed on first pass. Retrying once after clean to recover from stale generated state..."
if run_pass "Pass 2 (clean retry)" clean; then
  echo
  echo "Compile matrix validation passed after clean retry."
  exit 0
fi

echo
echo "Compile matrix validation failed. Fix compile errors before committing."
exit 1
