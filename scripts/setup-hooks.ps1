$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

git config --local core.hooksPath .githooks

# Ensure hooks are executable
@('.githooks/pre-commit', '.githooks/commit-msg', 
  'scripts/validate-docs.sh', 'scripts/validate-version-bump.sh', 'scripts/validate-changelog.sh', 
  'scripts/validate-optimization-pass.sh', 'scripts/validate-compile-matrix.sh',
  'scripts/validate-docs.ps1', 'scripts/validate-version-bump.ps1', 'scripts/validate-changelog.ps1',
  'scripts/validate-optimization-pass.ps1', 'scripts/validate-compile-matrix.ps1') | ForEach-Object {
    if (Test-Path $_) {
        icacls $_ /grant:r "$env:USERNAME`:F" 2>$null | Out-Null
    }
}

$configured = (git config --local --get core.hooksPath).Trim()
Write-Host "Configured repository hooks path: $configured" -ForegroundColor Green
Write-Host '✓ commit-msg hook: validates semantic commit format' -ForegroundColor Green
Write-Host '✓ pre-commit hook: runs docs, version-bump, changelog, optimization, and compile-matrix validators' -ForegroundColor Green
