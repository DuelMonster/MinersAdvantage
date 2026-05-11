$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$changelogPath = Join-Path $repoRoot 'CHANGELOG.md'

if (-not (Test-Path $changelogPath)) {
    Write-Host 'CHANGELOG.md is missing.' -ForegroundColor Red
    exit 1
}

$changelog = Get-Content -Raw -Path $changelogPath

# Check that CHANGELOG has at least one version header (e.g., ## 0.18.0)
if ($changelog -notmatch '## \d+\.\d+\.\d+') {
    Write-Host 'CHANGELOG.md missing version headers (format: ## X.Y.Z)' -ForegroundColor Yellow
    exit 1
}

# Check that there is at least one bullet entry under a version header
if ($changelog -notmatch '## \d+\.\d+\.\d+[\r\n]+.*?^-') {
    Write-Host 'CHANGELOG.md has version headers but no entries (format: - description)' -ForegroundColor Yellow
    exit 1
}

Write-Host 'CHANGELOG validation passed.' -ForegroundColor Green
exit 0
