$ErrorActionPreference = 'Stop'

$repoRoot = (git rev-parse --show-toplevel).Trim()
$docsScript = Join-Path $repoRoot 'scripts/validate-docs.ps1'
$versionScript = Join-Path $repoRoot 'scripts/validate-version-bump.ps1'
$optScript = Join-Path $repoRoot 'scripts/validate-optimization-pass.ps1'
$changelogScript = Join-Path $repoRoot 'scripts/validate-changelog.ps1'
$compileScript = Join-Path $repoRoot 'scripts/validate-compile-matrix.ps1'

if (-not (Test-Path $docsScript)) {
    Write-Host 'Missing docs validator: scripts/validate-docs.ps1' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $versionScript)) {
    Write-Host 'Missing version bump validator: scripts/validate-version-bump.ps1' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $optScript)) {
    Write-Host 'Missing optimization validator: scripts/validate-optimization-pass.ps1' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $changelogScript)) {
    Write-Host 'Missing changelog validator: scripts/validate-changelog.ps1' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $compileScript)) {
    Write-Host 'Missing compile validator: scripts/validate-compile-matrix.ps1' -ForegroundColor Red
    exit 1
}

& $docsScript
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $versionScript
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $optScript
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $changelogScript
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& $compileScript
exit $LASTEXITCODE
