$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$changelogPath = Join-Path $repoRoot 'CHANGELOG.md'

if (-not (Test-Path $changelogPath)) {
    Write-Host 'CHANGELOG.md is missing.' -ForegroundColor Red
    exit 1
}

$changelogLines = Get-Content -Path $changelogPath

$hasVersionHeader = $false
$hasBulletEntry = $false

foreach ($line in $changelogLines) {
    if ($line -match '^## \d+\.\d+\.\d+$') {
        $hasVersionHeader = $true
        continue
    }

    if ($hasVersionHeader -and $line -match '^\s*-\s+.+$') {
        $hasBulletEntry = $true
        break
    }
}

if (-not $hasVersionHeader) {
    Write-Host 'CHANGELOG.md missing version headers (format: ## X.Y.Z)' -ForegroundColor Yellow
    exit 1
}

if (-not $hasBulletEntry) {
    Write-Host 'CHANGELOG.md has version headers but no entries (format: - description)' -ForegroundColor Yellow
    exit 1
}

Write-Host 'CHANGELOG validation passed.' -ForegroundColor Green
exit 0
