param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-Java25Home {
    if ($env:JAVA25_HOME -and (Test-Path $env:JAVA25_HOME)) {
        return $env:JAVA25_HOME
    }

    if ($env:JDK25 -and (Test-Path $env:JDK25)) {
        return $env:JDK25
    }

    $candidates = @()
    $roots = @(
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Java"
    )

    foreach ($root in $roots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $matches = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match '^jdk-?25([\._-].*)?$' } |
            Sort-Object Name -Descending

        if ($matches) {
            $candidates += $matches
        }
    }

    if ($candidates.Count -gt 0) {
        return $candidates[0].FullName
    }

    return $null
}

$java25Home = Get-Java25Home
if (-not $java25Home) {
    Write-Error "Java 25 not found. Install JDK 25 and set JAVA25_HOME (or JDK25) before running Gradle tasks."
}

$gradleBat = Join-Path $PSScriptRoot "..\gradlew.bat"
$gradleBat = [System.IO.Path]::GetFullPath($gradleBat)

if (-not (Test-Path $gradleBat)) {
    Write-Error "Unable to locate gradlew.bat at $gradleBat"
}

$env:JAVA_HOME = $java25Home

Write-Host "Using JAVA_HOME=$java25Home"
& $gradleBat @GradleArgs
exit $LASTEXITCODE
