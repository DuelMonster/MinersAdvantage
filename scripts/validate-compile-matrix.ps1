$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

# Runs before the skip switch: node folders own only gradle.properties, everything else there is generated.
$strayNodeSources = @(
  Get-ChildItem -Path (Join-Path $repoRoot 'versions') -Directory -ErrorAction SilentlyContinue |
  Where-Object { Test-Path (Join-Path $_.FullName 'src') } |
  ForEach-Object { "versions/$($_.Name)/src" }
)

if ($strayNodeSources.Count -gt 0) {
  Write-Host 'Found source directories inside Stonecutter node folders:' -ForegroundColor Red
  foreach ($strayPath in $strayNodeSources) {
    Write-Host "- $strayPath" -ForegroundColor Red
  }
  Write-Host ''
  Write-Host 'Sources there bypass Stonecutter preprocessing and drift between nodes.' -ForegroundColor Red
  Write-Host 'Move them into src/main/java behind Stonecutter conditions (see TECHNICAL.md, Source Layout).' -ForegroundColor Red
  exit 1
}

if ($env:MA_SKIP_COMPILE_MATRIX -eq '1') {
  Write-Host 'Compile matrix validation skipped via MA_SKIP_COMPILE_MATRIX=1.' -ForegroundColor Yellow
  exit 0
}

$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
if (-not (Test-Path $gradleWrapper)) {
  $gradleWrapper = Join-Path $repoRoot 'gradlew'
}
if (-not (Test-Path $gradleWrapper)) {
  Write-Host 'Missing Gradle wrapper (gradlew/gradlew.bat).' -ForegroundColor Red
  exit 1
}

$versionsRoot = Join-Path $repoRoot 'versions'
if (-not (Test-Path $versionsRoot)) {
  Write-Host 'Missing versions directory; cannot run compile matrix validation.' -ForegroundColor Red
  exit 1
}

$nodes = @(Get-ChildItem -Path $versionsRoot -Directory | Where-Object {
    $_.Name -match '^\d+(?:\.\d+){1,3}-(fabric|neoforge)$' -and
    (Test-Path (Join-Path $_.FullName 'gradle.properties'))
  } | Sort-Object Name)

if ($nodes.Count -eq 0) {
  Write-Host 'No Stonecutter nodes found under versions/ (expected <mcVersion>-<loader> folders).' -ForegroundColor Red
  exit 1
}

$tasks = @()
foreach ($node in $nodes) {
  $tasks += ":$($node.Name):compileJava"
  $tasks += ":$($node.Name):compileTestJava"
}

$commonArgs = @('--no-daemon', '--console=plain')

Write-Host "Compile matrix validation will check $($nodes.Count) nodes:" -ForegroundColor Cyan
foreach ($node in $nodes) {
  Write-Host "- $($node.Name)" -ForegroundColor Cyan
}

function Invoke-CompileMatrixPass {
  param(
    [string]$Label,
    [string[]]$ExtraArgs
  )

  Write-Host ''
  Write-Host "[$Label] Running compile tasks..." -ForegroundColor Cyan
  & $gradleWrapper @commonArgs @ExtraArgs @tasks 2>&1 | Out-Host
  $exitCode = $LASTEXITCODE
  return ($exitCode -eq 0)
}

if (Invoke-CompileMatrixPass -Label 'Pass 1' -ExtraArgs @()) {
  Write-Host ''
  Write-Host 'Compile matrix validation passed.' -ForegroundColor Green
  exit 0
}

Write-Host ''
Write-Host 'Compile matrix failed on first pass. Retrying once after clean to recover from stale generated state...' -ForegroundColor Yellow
if (Invoke-CompileMatrixPass -Label 'Pass 2 (clean retry)' -ExtraArgs @('clean')) {
  Write-Host ''
  Write-Host 'Compile matrix validation passed after clean retry.' -ForegroundColor Green
  exit 0
}

Write-Host ''
Write-Host 'Compile matrix validation failed. Fix compile errors before committing.' -ForegroundColor Red
exit 1
