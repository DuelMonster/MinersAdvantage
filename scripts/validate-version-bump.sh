#!/usr/bin/env bash
set -euo pipefail

today="$(date +%F)"
version_file="gradle.properties"
state_file=".brainbox/state/version-bump-state.txt"
override_file=".brainbox/state/version-bump-override.txt"

get_staged_content() {
  git show ":$1" 2>/dev/null || true
}

get_head_content() {
  git show "HEAD:$1" 2>/dev/null || true
}

get_mod_version() {
  grep -E '^mod_version=' <<< "$1" | head -n1 | cut -d= -f2- | sed 's/^ *//; s/ *$//' || true
}

get_date_stamp() {
  grep -Eo '[0-9]{4}-[0-9]{2}-[0-9]{2}' <<< "$1" | head -n1 || true
}

staged_version_content="$(get_staged_content "$version_file")"
if [[ -z "$staged_version_content" ]]; then
  exit 0
fi

head_version_content="$(get_head_content "$version_file")"
if [[ -z "$head_version_content" ]]; then
  exit 0
fi

staged_version="$(get_mod_version "$staged_version_content")"
head_version="$(get_mod_version "$head_version_content")"

if [[ -z "$staged_version" || -z "$head_version" ]]; then
  echo 'Version bump validation failed: unable to read mod_version from gradle.properties.'
  exit 1
fi

if [[ "$staged_version" == "$head_version" ]]; then
  exit 0
fi

staged_state_content="$(get_staged_content "$state_file")"
if [[ -z "$staged_state_content" ]]; then
  echo "Version bump validation failed: stage $state_file alongside any mod_version change."
  exit 1
fi

head_state_content="$(get_head_content "$state_file")"
staged_state_date="$(get_date_stamp "$staged_state_content")"
head_state_date="$(get_date_stamp "$head_state_content")"
override_content="$(get_staged_content "$override_file")"
override_date="$(get_date_stamp "$override_content")"

if [[ "$staged_state_date" != "$today" ]]; then
  echo "Version bump validation failed: $state_file must be updated to today's date ($today) in the same commit as the bump."
  exit 1
fi

if [[ "$head_state_date" == "$today" && "$override_date" != "$today" ]]; then
  echo "Version bump validation failed: a bump already happened today. To override, stage $override_file with today's date and an explicit reason."
  exit 1
fi

echo 'Version bump validation passed.'
exit 0