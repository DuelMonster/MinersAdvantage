# 🧑‍💻 Semantic Commit Guidelines

This document outlines the commit message standards for Forget-Me-Crops to ensure clear, meaningful, and automatically-processable commit history.

## Quick Reference

**Format:** `TYPE: subject`

**Example:** `♻️refactor: simplify harvest logic`

### Valid Types

| Emoji | Type | Usage |
|-------|------|-------|
| ✨ | `feature` | Adding new functionality |
| 🐞 | `fix` | Bug fixes and defect corrections |
| ⛏️ | `minor` | Small changes to existing code |
| 🎨 | `style` | Code styling changes only (no logic change) |
| ♻️ | `refactor` | Code restructuring without functional changes |
| 🚧 | `wip` | Work-in-progress commits (use sparingly) |
| 📝 | `docs` | Documentation, comments, or README updates |
| ✅ | `test` | Test additions or modifications |
| 👷 | `build` | Build system, dependencies, CI/CD config |
| 🔁 | `merge` | Manual branch merges |
| 🧹 | `chore` | Routine maintenance and housekeeping |

## Rules

1. **Maximum 65 characters** for the subject line
2. **Start with the emoji and type** followed by a colon and space
3. **Use simple future tense** (e.g., "add" not "added")
4. **Be specific** - describe what and why, not just that a change occurred
5. **One logical change per commit** - keep commits focused

## Examples

### ✅ Good Commits

```
✨feature: implement seasonal crop support
🐞fix: prevent nil reference in frame validator
♻️refactor: extract validation patterns into utility
📝docs: update farming strategies guide
```

### ❌ Bad Commits

```
fix: stuff
updated things
refactored code
v1.2.0
```

## Optional: Commit Body

For complex changes, add a blank line after the subject, then explain:
- **Why** the change was needed
- **What** problem it solves
- **How** it works (if non-obvious)

Example:

```
♻️refactor: extract exception handling utilities

CatchupManager was using try-catch patterns repetitively.
ExceptionHandler consolidates these into reusable silentTry() methods,
reducing boilerplate by ~150 lines while improving clarity and consistency
across the harvest and frame-discovery pipelines.
```

## Automatic Enforcement

This repository uses a `commit-msg` git hook that validates all commits against the semantic format. If your commit message doesn't match the required format, git will reject it with helpful guidance.

### Running the Hook Manually

If needed, you can validate a commit message before pushing:

```bash
# Bash
bash .git/hooks/commit-msg <(echo "Your commit message here")

# PowerShell
.\.git\hooks\commit-msg.ps1 -CommitMsgFile (New-TemporaryFile)
```

## Tips for Better Commits

1. **Commit often** - smaller, focused commits are easier to understand and revert if needed
2. **Review before pushing** - use `git log --oneline` to check your commits
3. **Link issues** - reference issues in commit body: "Fixes #123" or "Relates to #456"
4. **Avoid WIP in production** - 🚧wip commits should be cleaned up before pushing to main
5. **Squash when appropriate** - combine trivial commits: `git rebase -i` to squash before merging

## Questions?

Refer to [commit.rules.md](./commit.rules.md) for the authoritative standard, or check the `.git/hooks/commit-msg` validation logic.

## Related Guide

For daily versioning policy, see [VERSION_BUMP_RULE.md](./VERSION_BUMP_RULE.md).
