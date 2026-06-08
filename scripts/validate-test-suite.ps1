$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$runner = Join-Path $repoRoot 'scripts/run-gradle-java25.ps1'

if (-not (Test-Path $runner)) {
    Write-Host 'Missing test runner: scripts/run-gradle-java25.ps1' -ForegroundColor Red
    exit 1
}

Write-Host 'Running full test suite before compile matrix...' -ForegroundColor Cyan
& $runner test
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host ''
    Write-Host 'Full test suite failed. The Gradle output above identifies the failing module, test class, and assertion or exception.' -ForegroundColor Red
    Write-Host 'To resolve it, fix the failing test or the affected production code, then rerun the test suite before committing.' -ForegroundColor Red
    Write-Host 'Suggested local check: pwsh -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-gradle-java25.ps1 test' -ForegroundColor Yellow
}

exit $exitCode