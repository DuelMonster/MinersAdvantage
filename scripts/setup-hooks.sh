#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

git config --local core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/commit-msg scripts/validate-docs.sh scripts/validate-changelog.sh scripts/validate-optimization-pass.sh 2>/dev/null || true

echo "Configured repository hooks path: $(git config --local --get core.hooksPath)"
echo "✓ commit-msg hook: validates semantic commit format"
echo "✓ pre-commit hook: runs docs, changelog, and optimization validators"
