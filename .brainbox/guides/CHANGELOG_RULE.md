# 📝 CHANGELOG Maintenance Guide

The CHANGELOG must be updated **before every commit** that introduces user-facing changes (features, fixes, refactors affecting behavior). This ensures accurate release notes and project history.

## Rules

1. **Update before commit** — Never commit code changes without first adding them to CHANGELOG.md
2. **Group by version** — All unreleased work goes under the current version number (bumped at release time)
3. **Format consistently** — Use a single hyphen bullet for each entry; single line per change
4. **Be user-focused** — Describe impact and behavior, not implementation details
5. **Link to TECHNICAL if needed** — For architectural changes, point to TECHNICAL.md for deep dives
6. **Omit purely internal refactors** — Only include changes visible to users or operators (unless significant architecture shift)

## Format

```markdown
## <Version>

- Brief description of user-visible change or fix
- Another change with impact
- Bug fixed that affects behavior or performance
```

## When NOT to Update

- Typo fixes in comments
- Internal variable renames with no external impact
- Pure style adjustments
- Test-only changes

## When TO Update

- ✨ New features or modes
- 🐞 Bug fixes affecting user experience or performance
- ♻️ Behavior changes in existing features (e.g., rotation semantics)
- 📝 Major documentation or config additions
- 👷 Build system changes affecting end users
- 🐞 Any fixes to rotation, harvesting, replanting, or core scan logic

## Examples

✅ **Good entries:**
- "Fix FOLLOW_ROTATION to properly cycle through all 8 steps per ring and return to start"
- "Add YACL configuration screen for in-game settings"
- "Prevent nil reference crashes in frame validator"

❌ **Bad entries:**
- "Updated FarmScanTask.java" (too vague, no user impact)
- "Refactored FrameScanner to use streams" (internal implementation)
- "Fixed typo in comment" (invisible to users)
