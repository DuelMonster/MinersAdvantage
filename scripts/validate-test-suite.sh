#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
runner="$repo_root/scripts/run-gradle-java25.ps1"

if [[ ! -f "$runner" ]]; then
  echo "Missing test runner: scripts/run-gradle-java25.ps1"
  exit 1
fi

echo "Running full test suite before compile matrix..."
set +e
pwsh -NoProfile -ExecutionPolicy Bypass -File "$runner" test
exit_code=$?
set -e

if [[ $exit_code -ne 0 ]]; then
  echo
  echo "Full test suite failed. The Gradle output above identifies the failing module, test class, and assertion or exception."
  echo "To resolve it, fix the failing test or the affected production code, then rerun the test suite before committing."
  echo "Suggested local check: pwsh -NoProfile -ExecutionPolicy Bypass -File ./scripts/run-gradle-java25.ps1 test"
fi

exit $exit_code