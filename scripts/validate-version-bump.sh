#!/usr/bin/env bash
set -euo pipefail

today="$(date +%F)"
version_file="gradle.properties"
state_file=".brainbox/state/version-bump-state.txt"
override_file=".brainbox/state/version-bump-override.txt"
changelog_file="CHANGELOG.md"

get_staged_content() {
  git show ":$1" 2>/dev/null || true
}

get_head_content() {
  git show "HEAD:$1" 2>/dev/null || true
}

get_commit_content() {
  local staged_content
  staged_content="$(get_staged_content "$1")"
  if [[ -n "$staged_content" ]]; then
    echo "$staged_content"
    return
  fi

  get_head_content "$1"
}

get_mod_version() {
  grep -E '^mod_version=' <<< "$1" | head -n1 | cut -d= -f2- | sed 's/^ *//; s/ *$//' || true
}

get_date_stamp() {
  grep -Eo '[0-9]{4}-[0-9]{2}-[0-9]{2}' <<< "$1" | head -n1 || true
}

get_next_version() {
  grep -E '^Next Version:[[:space:]]*[0-9]+\.[0-9]+\.[0-9]+([[:space:]]|$)' <<< "$1" | head -n1 | sed -E 's/^Next Version:[[:space:]]*([0-9]+\.[0-9]+\.[0-9]+).*/\1/' || true
}

is_semver_gt() {
  local left="$1"
  local right="$2"
  local lmaj lmin lpat rmaj rmin rpat

  IFS=. read -r lmaj lmin lpat <<< "$left"
  IFS=. read -r rmaj rmin rpat <<< "$right"

  if (( lmaj > rmaj )); then
    return 0
  fi

  if (( lmaj < rmaj )); then
    return 1
  fi

  if (( lmin > rmin )); then
    return 0
  fi

  if (( lmin < rmin )); then
    return 1
  fi

  if (( lpat > rpat )); then
    return 0
  fi

  return 1
}

get_first_changelog_version() {
  grep -E '^##[[:space:]]+[0-9]+\.[0-9]+\.[0-9]+([[:space:]]|$)' <<< "$1" | head -n1 | sed -E 's/^##[[:space:]]+([0-9]+\.[0-9]+\.[0-9]+).*/\1/' || true
}

require_today_state_date() {
  local commit_state_content
  local commit_state_date

  commit_state_content="$(get_commit_content "$state_file")"
  if [[ -z "$commit_state_content" ]]; then
    echo "Version bump validation failed: unable to read $state_file from staged content or HEAD."
    exit 1
  fi

  commit_state_date="$(get_date_stamp "$commit_state_content")"
  if [[ "$commit_state_date" != "$today" ]]; then
    echo "Version bump validation failed: $state_file must contain today's date ($today) for every commit."
    exit 1
  fi
}

require_state_next_version() {
  local state_content="$1"
  local parsed_next

  parsed_next="$(get_next_version "$state_content")"
  if [[ -z "$parsed_next" ]]; then
    echo "Version bump validation failed: $state_file must include a 'Next Version: X.Y.Z' entry."
    exit 1
  fi

  echo "$parsed_next"
}

require_new_changelog_section_for_bump() {
  local staged_changelog_content
  local head_changelog_content
  local staged_top_version
  local head_top_version

  staged_changelog_content="$(get_staged_content "$changelog_file")"
  if [[ -z "$staged_changelog_content" ]]; then
    echo "Version bump validation failed: stage $changelog_file with a new top version section (## $staged_version) alongside any mod_version bump."
    exit 1
  fi

  staged_top_version="$(get_first_changelog_version "$staged_changelog_content")"
  if [[ "$staged_top_version" != "$staged_version" ]]; then
    echo "Version bump validation failed: the first version section in $changelog_file must be '## $staged_version' when mod_version is bumped."
    exit 1
  fi

  head_changelog_content="$(get_head_content "$changelog_file")"
  if [[ -n "$head_changelog_content" ]]; then
    head_top_version="$(get_first_changelog_version "$head_changelog_content")"
    if [[ "$head_top_version" == "$staged_top_version" ]]; then
      echo "Version bump validation failed: add a new top version section to $changelog_file for bumped version $staged_version."
      exit 1
    fi
  fi
}

commit_state_content="$(get_commit_content "$state_file")"
if [[ -z "$commit_state_content" ]]; then
  echo "Version bump validation failed: unable to read $state_file from staged content or HEAD."
  exit 1
fi

require_today_state_date
commit_next_version="$(require_state_next_version "$commit_state_content")"

head_state_content="$(get_head_content "$state_file")"
head_state_date="$(get_date_stamp "$head_state_content")"
head_next_version="$(get_next_version "$head_state_content")"

head_version_content="$(get_head_content "$version_file")"
if [[ -z "$head_version_content" ]]; then
  exit 0
fi

head_version="$(get_mod_version "$head_version_content")"
if [[ -z "$head_version" ]]; then
  echo 'Version bump validation failed: unable to read mod_version from gradle.properties from HEAD.'
  exit 1
fi

staged_version_content="$(get_staged_content "$version_file")"
version_changed='false'
staged_version="$head_version"

if [[ -n "$staged_version_content" ]]; then
  staged_version="$(get_mod_version "$staged_version_content")"
  if [[ -z "$staged_version" ]]; then
    echo 'Version bump validation failed: unable to read staged mod_version from gradle.properties.'
    exit 1
  fi

  if [[ "$staged_version" != "$head_version" ]]; then
    version_changed='true'
  fi
fi

staged_state_content="$(get_staged_content "$state_file")"

if [[ "$head_state_date" != "$today" ]]; then
  if [[ "$version_changed" != 'true' ]]; then
    echo "Version bump validation failed: first commit of the day must bump mod_version from $head_version."
    exit 1
  fi

  if [[ -z "$staged_state_content" ]]; then
    echo "Version bump validation failed: stage $state_file when advancing the daily bump date."
    exit 1
  fi

  if [[ -n "$head_next_version" ]]; then
    if [[ "$staged_version" != "$head_next_version" ]]; then
      echo "Version bump validation failed: staged mod_version must equal expected Next Version ($head_next_version)."
      exit 1
    fi
  elif ! is_semver_gt "$staged_version" "$head_version"; then
    echo "Version bump validation failed: staged mod_version ($staged_version) must be greater than HEAD version ($head_version)."
    exit 1
  fi

  if ! is_semver_gt "$commit_next_version" "$staged_version"; then
    echo "Version bump validation failed: state Next Version ($commit_next_version) must be greater than bumped mod_version ($staged_version)."
    exit 1
  fi

  require_new_changelog_section_for_bump
  echo 'Version bump validation passed.'
  exit 0
fi

if [[ "$version_changed" != 'true' ]]; then
  echo 'Version bump validation passed.'
  exit 0
fi

if [[ -z "$staged_state_content" ]]; then
  echo "Version bump validation failed: stage $state_file alongside any mod_version change."
  exit 1
fi

override_content="$(get_staged_content "$override_file")"
override_date="$(get_date_stamp "$override_content")"
if [[ "$override_date" != "$today" ]]; then
  echo "Version bump validation failed: a bump already happened today. To override, stage $override_file with today's date and an explicit reason."
  exit 1
fi

if [[ -n "$head_next_version" && "$staged_version" != "$head_next_version" ]]; then
  echo "Version bump validation failed: staged mod_version must equal expected Next Version ($head_next_version)."
  exit 1
fi

if ! is_semver_gt "$commit_next_version" "$staged_version"; then
  echo "Version bump validation failed: state Next Version ($commit_next_version) must be greater than bumped mod_version ($staged_version)."
  exit 1
fi

require_new_changelog_section_for_bump

echo 'Version bump validation passed.'
exit 0