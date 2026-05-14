$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$changelogPath = Join-Path $repoRoot 'CHANGELOG.md'

if (-not (Test-Path $changelogPath)) {
    Write-Host 'CHANGELOG.md is missing.' -ForegroundColor Red
    exit 1
}

$changelogLines = Get-Content -Path $changelogPath
$stagedFiles = @(git -C $repoRoot diff --cached --name-only --diff-filter=ACMR)

if ($stagedFiles.Count -eq 0) {
    Write-Host 'CHANGELOG validation passed.' -ForegroundColor Green
    exit 0
}

$changelogStaged = $false
$requiresChangelogUpdate = $false

foreach ($file in $stagedFiles) {
    $normalized = ($file -replace '\\', '/').Trim()
    if ($normalized -eq 'CHANGELOG.md') {
        $changelogStaged = $true
        continue
    }

    if ($normalized -eq '.brainbox/state/version-bump-state.txt') {
        continue
    }

    $requiresChangelogUpdate = $true
}

if ($requiresChangelogUpdate -and -not $changelogStaged) {
    Write-Host 'CHANGELOG update required: stage CHANGELOG.md when committing substantive changes.' -ForegroundColor Red
    exit 1
}

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
