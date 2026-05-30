$ErrorActionPreference = 'Stop'

$today = Get-Date -Format 'yyyy-MM-dd'
$relativeVersionFile = 'gradle.properties'
$relativeStateFile = '.brainbox/state/version-bump-state.txt'
$relativeOverrideFile = '.brainbox/state/version-bump-override.txt'
$relativeChangelogFile = 'CHANGELOG.md'

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

function Get-CommitContent {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RelativePath
    )

    $stagedContent = Get-StagedContent -RelativePath $RelativePath
    if ($null -ne $stagedContent) {
        return $stagedContent
    }

    return Get-HeadContent -RelativePath $RelativePath
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

function Get-NextVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    foreach ($line in $Content -split "`r?`n") {
        if ($line -match '^Next Version:\s*([0-9]+\.[0-9]+\.[0-9]+)(\s|$)') {
            return $Matches[1]
        }
    }

    return $null
}

function Test-SemVerGreater {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Left,
        [Parameter(Mandatory = $true)]
        [string]$Right
    )

    $leftParts = $Left.Split('.') | ForEach-Object { [int]$_ }
    $rightParts = $Right.Split('.') | ForEach-Object { [int]$_ }

    for ($index = 0; $index -lt 3; $index++) {
        if ($leftParts[$index] -gt $rightParts[$index]) {
            return $true
        }

        if ($leftParts[$index] -lt $rightParts[$index]) {
            return $false
        }
    }

    return $false
}

function Get-FirstChangelogVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    foreach ($line in $Content -split "`r?`n") {
        if ($line -match '^##\s+([0-9]+\.[0-9]+\.[0-9]+)(\s|$)') {
            return $Matches[1]
        }
    }

    return $null
}

function Assert-TodayStateDate {
    $commitStateContent = Get-CommitContent -RelativePath $relativeStateFile
    if ($null -eq $commitStateContent) {
        Write-Host "Version bump validation failed: unable to read $relativeStateFile from staged content or HEAD." -ForegroundColor Red
        exit 1
    }

    $commitStateDate = Get-DateStamp -Content $commitStateContent
    if ($commitStateDate -ne $today) {
        Write-Host "Version bump validation failed: $relativeStateFile must contain today's date ($today) for every commit." -ForegroundColor Red
        exit 1
    }
}

function Assert-StateNextVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$StateContent
    )

    $nextVersion = Get-NextVersion -Content $StateContent
    if ($null -eq $nextVersion) {
        Write-Host "Version bump validation failed: $relativeStateFile must include a 'Next Version: X.Y.Z' entry." -ForegroundColor Red
        exit 1
    }

    return $nextVersion
}

function Assert-NewChangelogSectionForBump {
    param(
        [Parameter(Mandatory = $true)]
        [string]$StagedVersion
    )

    $stagedChangelogContent = Get-StagedContent -RelativePath $relativeChangelogFile
    if ($null -eq $stagedChangelogContent) {
        Write-Host "Version bump validation failed: stage $relativeChangelogFile with a new top version section (## $StagedVersion) alongside any mod_version bump." -ForegroundColor Red
        exit 1
    }

    $stagedTopVersion = Get-FirstChangelogVersion -Content $stagedChangelogContent
    if ($stagedTopVersion -ne $StagedVersion) {
        Write-Host "Version bump validation failed: the first version section in $relativeChangelogFile must be '## $StagedVersion' when mod_version is bumped." -ForegroundColor Red
        exit 1
    }

    $headChangelogContent = Get-HeadContent -RelativePath $relativeChangelogFile
    if ($null -ne $headChangelogContent) {
        $headTopVersion = Get-FirstChangelogVersion -Content $headChangelogContent
        if ($headTopVersion -eq $stagedTopVersion) {
            Write-Host "Version bump validation failed: add a new top version section to $relativeChangelogFile for bumped version $StagedVersion." -ForegroundColor Red
            exit 1
        }
    }
}

$commitStateContent = Get-CommitContent -RelativePath $relativeStateFile
if ($null -eq $commitStateContent) {
    Write-Host "Version bump validation failed: unable to read $relativeStateFile from staged content or HEAD." -ForegroundColor Red
    exit 1
}

Assert-TodayStateDate
$commitNextVersion = Assert-StateNextVersion -StateContent $commitStateContent

$headStateContent = Get-HeadContent -RelativePath $relativeStateFile
$headStateDate = $null
$headNextVersion = $null
if ($null -ne $headStateContent) {
    $headStateDate = Get-DateStamp -Content $headStateContent
    $headNextVersion = Get-NextVersion -Content $headStateContent
}

$headVersionContent = Get-HeadContent -RelativePath $relativeVersionFile
if ($null -eq $headVersionContent) {
    exit 0
}

$headVersion = Get-ModVersion -Content $headVersionContent
if ($null -eq $headVersion) {
    Write-Host 'Version bump validation failed: unable to read mod_version from gradle.properties from HEAD.' -ForegroundColor Red
    exit 1
}

$stagedVersionContent = Get-StagedContent -RelativePath $relativeVersionFile
$stagedVersion = $headVersion
$versionChanged = $false
if ($null -ne $stagedVersionContent) {
    $stagedVersion = Get-ModVersion -Content $stagedVersionContent
    if ($null -eq $stagedVersion) {
        Write-Host 'Version bump validation failed: unable to read staged mod_version from gradle.properties.' -ForegroundColor Red
        exit 1
    }

    $versionChanged = $stagedVersion -ne $headVersion
}

$stagedStateContent = Get-StagedContent -RelativePath $relativeStateFile

if ($headStateDate -ne $today) {
    if (-not $versionChanged) {
        Write-Host "Version bump validation failed: first commit of the day must bump mod_version from $headVersion." -ForegroundColor Red
        exit 1
    }

    if ($null -eq $stagedStateContent) {
        Write-Host "Version bump validation failed: stage $relativeStateFile when advancing the daily bump date." -ForegroundColor Red
        exit 1
    }

    if ($null -ne $headNextVersion) {
        if ($stagedVersion -ne $headNextVersion) {
            Write-Host "Version bump validation failed: staged mod_version must equal expected Next Version ($headNextVersion)." -ForegroundColor Red
            exit 1
        }
    } elseif (-not (Test-SemVerGreater -Left $stagedVersion -Right $headVersion)) {
        Write-Host "Version bump validation failed: staged mod_version ($stagedVersion) must be greater than HEAD version ($headVersion)." -ForegroundColor Red
        exit 1
    }

    if (-not (Test-SemVerGreater -Left $commitNextVersion -Right $stagedVersion)) {
        Write-Host "Version bump validation failed: state Next Version ($commitNextVersion) must be greater than bumped mod_version ($stagedVersion)." -ForegroundColor Red
        exit 1
    }

    Assert-NewChangelogSectionForBump -StagedVersion $stagedVersion
    Write-Host 'Version bump validation passed.' -ForegroundColor Green
    exit 0
}

if (-not $versionChanged) {
    Write-Host 'Version bump validation passed.' -ForegroundColor Green
    exit 0
}

if ($null -eq $stagedStateContent) {
    Write-Host 'Version bump validation failed: stage .brainbox/state/version-bump-state.txt alongside any mod_version change.' -ForegroundColor Red
    exit 1
}

$stagedOverrideContent = Get-StagedContent -RelativePath $relativeOverrideFile
$overrideDate = $null
if ($null -ne $stagedOverrideContent) {
    $overrideDate = Get-DateStamp -Content $stagedOverrideContent
}

if ($overrideDate -ne $today) {
    Write-Host "Version bump validation failed: a bump already happened today. To override, stage $relativeOverrideFile with today's date and an explicit reason." -ForegroundColor Red
    exit 1
}

if ($null -ne $headNextVersion -and $stagedVersion -ne $headNextVersion) {
    Write-Host "Version bump validation failed: staged mod_version must equal expected Next Version ($headNextVersion)." -ForegroundColor Red
    exit 1
}

if (-not (Test-SemVerGreater -Left $commitNextVersion -Right $stagedVersion)) {
    Write-Host "Version bump validation failed: state Next Version ($commitNextVersion) must be greater than bumped mod_version ($stagedVersion)." -ForegroundColor Red
    exit 1
}

Assert-NewChangelogSectionForBump -StagedVersion $stagedVersion

Write-Host 'Version bump validation passed.' -ForegroundColor Green
exit 0