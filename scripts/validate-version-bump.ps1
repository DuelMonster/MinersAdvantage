$ErrorActionPreference = 'Stop'

$repoRoot = (git rev-parse --show-toplevel).Trim()
$today = Get-Date -Format 'yyyy-MM-dd'
$relativeVersionFile = 'gradle.properties'
$relativeStateFile = '.brainbox/state/version-bump-state.txt'
$relativeOverrideFile = '.brainbox/state/version-bump-override.txt'

function Get-StagedContent {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RelativePath
    )

    $content = git show ":$RelativePath" 2>$null | Out-String
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return $content.TrimEnd()
}

function Get-HeadContent {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RelativePath
    )

    $content = git show "HEAD:$RelativePath" 2>$null | Out-String
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return $content.TrimEnd()
}

function Get-ModVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    foreach ($line in $Content -split "`r?`n") {
        if ($line -match '^mod_version=(.+)$') {
            return $Matches[1].Trim()
        }
    }

    return $null
}

function Get-DateStamp {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    if ($Content -match '(\d{4}-\d{2}-\d{2})') {
        return $Matches[1]
    }

    return $null
}

$stagedVersionContent = Get-StagedContent -RelativePath $relativeVersionFile
if ($null -eq $stagedVersionContent) {
    exit 0
}

$headVersionContent = Get-HeadContent -RelativePath $relativeVersionFile
if ($null -eq $headVersionContent) {
    exit 0
}

$stagedVersion = Get-ModVersion -Content $stagedVersionContent
$headVersion = Get-ModVersion -Content $headVersionContent

if ($null -eq $stagedVersion -or $null -eq $headVersion) {
    Write-Host 'Version bump validation failed: unable to read mod_version from gradle.properties.' -ForegroundColor Red
    exit 1
}

if ($stagedVersion -eq $headVersion) {
    exit 0
}

$stagedStateContent = Get-StagedContent -RelativePath $relativeStateFile
if ($null -eq $stagedStateContent) {
    Write-Host 'Version bump validation failed: stage .brainbox/state/version-bump-state.txt alongside any mod_version change.' -ForegroundColor Red
    exit 1
}

$headStateContent = Get-HeadContent -RelativePath $relativeStateFile
$stagedStateDate = Get-DateStamp -Content $stagedStateContent
$headStateDate = $null
if ($null -ne $headStateContent) {
    $headStateDate = Get-DateStamp -Content $headStateContent
}

$stagedOverrideContent = Get-StagedContent -RelativePath $relativeOverrideFile
$overrideDate = $null
if ($null -ne $stagedOverrideContent) {
    $overrideDate = Get-DateStamp -Content $stagedOverrideContent
}

if ($stagedStateDate -ne $today) {
    Write-Host "Version bump validation failed: $relativeStateFile must be updated to today's date ($today) in the same commit as the bump." -ForegroundColor Red
    exit 1
}

if ($headStateDate -eq $today -and $overrideDate -ne $today) {
    Write-Host "Version bump validation failed: a bump already happened today. To override, stage $relativeOverrideFile with today's date and an explicit reason." -ForegroundColor Red
    exit 1
}

Write-Host 'Version bump validation passed.' -ForegroundColor Green
exit 0