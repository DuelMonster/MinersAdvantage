$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$readmePath = Join-Path $repoRoot 'README.md'
$technicalPath = Join-Path $repoRoot 'TECHNICAL.md'

if (-not (Test-Path $readmePath)) {
    Write-Error 'README.md is missing.'
}

if (-not (Test-Path $technicalPath)) {
    Write-Error 'TECHNICAL.md is missing.'
}

$readme = Get-Content -Raw -Path $readmePath
$technical = Get-Content -Raw -Path $technicalPath

$requiredReadmeSections = @(
    '## Overview',
    '## Features',
    '## How It Works',
    '## Supported Crops',
    '## Installation',
    '## Configuration',
    '## Compatibility',
    '## Technical Documentation',
    '## License',
    '## Credits'
)

$requiredTechnicalSections = @(
    '## Build Instructions',
    '## License',
    '## Credits'
)

$missing = New-Object System.Collections.Generic.List[string]

foreach ($section in $requiredReadmeSections) {
    if ($readme -notmatch [regex]::Escape($section)) {
        $missing.Add("README missing required section: $section")
    }
}

foreach ($section in $requiredTechnicalSections) {
    if ($technical -notmatch [regex]::Escape($section)) {
        $missing.Add("TECHNICAL missing required section: $section")
    }
}

if ($readme -notmatch '\[TECHNICAL\.md\]\(TECHNICAL\.md\)') {
    $missing.Add('README missing link to TECHNICAL.md in markdown link format.')
}

if ($readme -notmatch '(?im)^MIT\b') {
    $missing.Add('README must explicitly state MIT license.')
}

if ($technical -notmatch '(?im)^MIT\b') {
    $missing.Add('TECHNICAL must explicitly state MIT license.')
}

# Installation section must cover both loaders
if ($readme -match '## Installation') {
    if ($readme -notmatch '(?i)\bFabric\b') {
        $missing.Add('README Installation section must mention Fabric.')
    }
    if ($readme -notmatch '(?i)\bNeoForge\b') {
        $missing.Add('README Installation section must mention NeoForge.')
    }
}

if ($missing.Count -gt 0) {
    Write-Host ''
    Write-Host 'Documentation validation failed:' -ForegroundColor Red
    foreach ($issue in $missing) {
        Write-Host "- $issue" -ForegroundColor Yellow
    }
    Write-Host ''
    Write-Host 'Fix documentation to satisfy .brainbox/guides/DOCUMENTATION_STANDARDS.md before committing.' -ForegroundColor Red
    exit 1
}

Write-Host 'Documentation validation passed.' -ForegroundColor Green
exit 0
