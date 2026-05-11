$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$staged = @(git diff --cached --name-only --diff-filter=ACMR)
if (-not $staged -or $staged.Count -eq 0) {
    Write-Host 'Optimization validation skipped: no staged files.' -ForegroundColor Yellow
    exit 0
}

$stagedPaths = @()
foreach ($path in $staged) {
    if ([string]::IsNullOrWhiteSpace($path)) { continue }
    $stagedPaths += (Join-Path $repoRoot $path)
}

$javaFiles = @($staged | Where-Object { $_ -like '*.java' })
$sourceFiles = @($staged | Where-Object { $_ -match '^(src/main|src/test)/' })

$errors = New-Object System.Collections.Generic.List[string]

foreach ($relative in $javaFiles) {
    $fullPath = Join-Path $repoRoot $relative
    if (-not (Test-Path $fullPath)) { continue }

    $lines = Get-Content -Path $fullPath

    $imports = @($lines | Where-Object { $_ -match '^\s*import\s+.+;\s*$' } | ForEach-Object { $_.Trim() })
    if ($imports.Count -gt 0) {
        $duplicateImports = $imports | Group-Object | Where-Object { $_.Count -gt 1 } | Select-Object -ExpandProperty Name
        foreach ($dup in $duplicateImports) {
            $errors.Add("$relative has duplicate import: $dup")
        }

        foreach ($imp in $imports) {
            if ($imp -match '^import\s+.+\.\*;\s*$') {
                $errors.Add("$relative uses wildcard import: $imp")
            }
        }
    }

    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match 'TODO|FIXME|XXX') {
            $errors.Add("$relative contains TODO/FIXME/XXX marker at line $($i + 1)")
        }
        if ($lines[$i] -match '\s+$') {
            $errors.Add("$relative has trailing whitespace at line $($i + 1)")
        }
    }

    $catchIgnoredMatches = @($lines | Where-Object { $_ -match 'catch\s*\(\s*Throwable\s+ignored\s*\)' })
    foreach ($match in $catchIgnoredMatches) {
        $lineNum = [array]::IndexOf($lines, $match) + 1
        $errors.Add("$relative uses catch(Throwable ignored) at line $lineNum; use util.ExceptionHandler instead")
    }
}

foreach ($relative in $sourceFiles) {
    $fullPath = Join-Path $repoRoot $relative
    if (-not (Test-Path $fullPath)) { continue }
    $lines = Get-Content -Path $fullPath
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '\s+$') {
            $errors.Add("$relative has trailing whitespace at line $($i + 1)")
        }
    }
}

if ($errors.Count -gt 0) {
    Write-Host ''
    Write-Host 'Optimization validation failed:' -ForegroundColor Red
    foreach ($e in $errors) {
        Write-Host "- $e" -ForegroundColor Red
    }
    Write-Host ''
    Write-Host 'Fix staged issues before committing. Rule source: .brainbox/rules/todo.optimisation.pass.md' -ForegroundColor Red
    exit 1
}

Write-Host 'Optimization validation passed.' -ForegroundColor Green
exit 0
