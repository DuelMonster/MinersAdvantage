$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

git config --local core.hooksPath .githooks

# Ensure hooks are executable
@('.githooks/pre-commit', '.githooks/commit-msg', 
  'scripts/validate-docs.sh', 'scripts/validate-changelog.sh', 
  'scripts/validate-optimization-pass.sh') | ForEach-Object {
    if (Test-Path $_) {
        icacls $_ /grant:r "$env:USERNAME`:F" 2>$null | Out-Null
    }
}

$configured = (git config --local --get core.hooksPath).Trim()
Write-Host "Configured repository hooks path: $configured" -ForegroundColor Green
Write-Host '✓ commit-msg hook: validates semantic commit format' -ForegroundColor Green
Write-Host '✓ pre-commit hook: runs docs, changelog, and optimization validators' -ForegroundColor Green
