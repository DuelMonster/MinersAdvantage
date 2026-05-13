---
name: commit-outstanding-semantic
description: 'Commit all outstanding workspace changes using semantic commit standards, including commit typing, message validation, scope grouping, and completion checks. Use when asked to finalize pending git changes, prepare clean history, or enforce emoji commit formats.'
argument-hint: 'What pending changes should be committed, and in how many logical commits?'
user-invocable: true
disable-model-invocation: false
---

# Commit Outstanding Changes With Semantic Standards

## When to Use
- User asks to commit pending files.
- Team requires semantic commits with emoji prefixes.
- You need a repeatable commit workflow with validation gates.

## Inputs
- Commit intent and expected logical grouping.
- Commit rules source files:
  - .brainbox/rules/commit.rules.md
  - .brainbox/guides/COMMIT_STANDARDS.md
- Current git working tree state.

## Procedure
1. Inspect repository state.
- Run status checks to list tracked and untracked changes.
- Confirm whether there are unrelated changes you must not revert.

2. Classify changes into logical commit groups.
- Group by purpose (feature, fix, refactor, docs, build, test, chore).
- Keep commits focused on one logical outcome each.
- Always split commits by logical type. Do not collapse mixed-purpose changes into a single commit.

3. Select semantic type and subject.
- Use emoji + type + ": " + short subject.
- Keep subject concise and action-oriented.
- Prefer simple imperative wording.
- Stay within stricter local limits when multiple standards conflict.

4. Stage files per commit group.
- Stage only files that belong to the current logical group.
- Re-check staged diff to verify scope.

5. Commit with semantic message.
- Use the selected semantic header.
- Add body text when context is non-obvious.
- Include footer references when required by team workflow.

6. Validate result.
- Confirm working tree is clean or only contains intentionally uncommitted files.
- Confirm commit log shows expected semantic header format.
- Report what was committed and any remaining risks.

## Decision Points
- Commit grouping policy:
  - Multiple commit groups are required when changes have different intent.
  - Single-commit flow is allowed only when all files truly represent one type and one purpose.
- Type selection:
  - `✨feature` for new functionality.
  - `🐞fix` for behavior correction.
  - `♻️refactor` for structural non-behavioral improvements.
  - `📝docs` for documentation-only changes.
  - `👷build` for build tooling or dependency setup.
  - `✅test` for tests.
  - `⛏️minor` or `🧹chore` for small maintenance changes.

## Completion Checks
- Every pending file requested by the user is either committed or explicitly called out.
- Commit message format matches semantic rules.
- Commit subjects are short, specific, and scan-friendly.
- No accidental reverts of unrelated user work.

## Quick Prompt Examples
- `/commit-outstanding-semantic commit all pending files with proper semantic types`
- `/commit-outstanding-semantic split pending changes into refactor and docs commits`
- `/commit-outstanding-semantic group and commit by feature/fix/docs/build types`
