$ErrorActionPreference = 'Stop'

$CommitMsgFile = $args[0]
if (-not $CommitMsgFile) {
    Write-Host 'ERROR: No commit message file provided' -ForegroundColor Red
    exit 1
}

$CommitMsg = Get-Content -Raw -Path $CommitMsgFile
$CommitSubject = (Get-Content -Path $CommitMsgFile | Select-Object -First 1).Trim()

if ([string]::IsNullOrWhiteSpace($CommitMsg)) {
    Write-Host 'ERROR: Empty commit message' -ForegroundColor Red
    exit 1
}

# Pattern: TYPE: subject (max 65 chars)
# Valid types: ✨feature, 🐞fix, ⛏minor, 🎨style, ♻️refactor, 🚧wip, 📝docs, ✅test, 👷build, 🔁merge, 🧹chore
$ValidTypes = @('✨feature', '🐞fix', '⛏minor', '🎨style', '♻️refactor', '🚧wip', '📝docs', '✅test', '👷build', '🔁merge', '🧹chore')
$TypePattern = $ValidTypes -join '|'
$ValidPattern = "^($TypePattern):\s+.{1,65}$"

if ($CommitSubject -notmatch $ValidPattern) {
    Write-Host ''
    Write-Host 'ERROR: Invalid commit message format' -ForegroundColor Red
    Write-Host ''
    Write-Host 'Your commit message:' -ForegroundColor Yellow
    Write-Host "  $CommitSubject"
    Write-Host ''
    Write-Host 'Valid semantic commit types:' -ForegroundColor Cyan
    Write-Host '  ✨feature  - Adding additional functionality'
    Write-Host '  🐞fix      - Defect fixes'
    Write-Host '  ⛏minor    - Small changes to existing code'
    Write-Host '  🎨style    - Changes affecting styling only'
    Write-Host '  ♻️refactor  - Code changes without functional changes'
    Write-Host '  🚧wip      - Code in an unfinished state'
    Write-Host '  📝docs     - Documentation updates'
    Write-Host '  ✅test     - Unit tests'
    Write-Host '  👷build    - Build system and package dependencies'
    Write-Host '  🔁merge    - Manual branch merges'
    Write-Host '  🧹chore    - Routine tasks and housekeeping'
    Write-Host ''
    Write-Host 'Format: TYPE: subject (max 65 chars)' -ForegroundColor Cyan
    Write-Host '  Example: ♻️refactor: simplify harvest logic'
    Write-Host ''
    exit 1
}

exit 0
