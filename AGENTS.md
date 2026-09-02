# Agent Instructions — MinersAdvantage

Multi-loader Minecraft mod built with Stonecutter + Modstitch. One shared source tree in `src/main/java`
is preprocessed into six nodes: `1.21.11`, `26.1.2`, `26.2` × `fabric`, `neoforge`.

Authoritative rules live in [.brainbox/rules](.brainbox/rules) and [.brainbox/guides](.brainbox/guides).
This file is the summary an agent must follow; the guides win if they disagree.

## Non-Negotiables

1. **Never bypass the git hooks.** No `--no-verify`. If a validator fails, fix the cause.
2. **Never commit without the user's confirmation** when the change affects runtime behaviour and has
   not been validated in game. Docs-only commits are exempt.
3. **Never commit unrelated working-tree changes.** Stage only the files you touched.

## Commands

| Purpose | Command |
| --- | --- |
| Compile every node (required gate) | `.\scripts\validate-compile-matrix.ps1` |
| Full test suite | `.\scripts\validate-test-suite.ps1` |
| Docs / changelog / version / optimization gates | `.\scripts\validate-docs.ps1`, `validate-changelog.ps1`, `validate-version-bump.ps1`, `validate-optimization-pass.ps1` |
| Build all nodes | `.\gradlew.bat chiseledBuild` |
| Preview release notes | `.\gradlew.bat :1.21.11-fabric:printReleaseChangelog` |
| Publish all nodes | `.\gradlew.bat chiseledPublishAll` |

Compile checks must always cover the **full** loader/version matrix, never a single node.
See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for the end-to-end release flow.

## Version Bumps

See [.brainbox/guides/VERSION_BUMP_RULE.md](.brainbox/guides/VERSION_BUMP_RULE.md).

- On the first code or content change of a day, bump `mod_version` minor by exactly **1**. Never more.
- The bumped value must equal the `Next Version` recorded in
  [.brainbox/state/version-bump-state.txt](.brainbox/state/version-bump-state.txt).
- In the same commit, set `Last Bumped Date` to today and advance `Next Version` past the bumped value.
- A second bump on the same day requires a staged
  [.brainbox/state/version-bump-override.txt](.brainbox/state/version-bump-override.txt) with today's
  date and an explicit reason, and only when the user asks for it.

## Changelog

See [.brainbox/guides/CHANGELOG_RULE.md](.brainbox/guides/CHANGELOG_RULE.md).

- Every substantive commit must stage `CHANGELOG.md`; the hook enforces this.
- When `mod_version` is bumped, immediately open a matching `## X.Y.Z` section and file all later
  entries under it.
- Describe user-visible effect, not the mechanics. Never write "Bump mod version" entries.
- Release notes uploaded to Modrinth and CurseForge are every `## X.Y.Z` section newer than the version
  already live on Modrinth, so entries must be accurate at publish time.

## Documentation

See [.brainbox/rules/documentation.rules.md](.brainbox/rules/documentation.rules.md).

- Keep `README.md`, `TECHNICAL.md` and, when throughput or replay policy changes,
  `TUNING_MATRIX.md` in step with code changes in the same commit.
- Pad Markdown tables so they stay readable as raw text.

## Comments

See [.brainbox/guides/COMMENT_STYLE.md](.brainbox/guides/COMMENT_STYLE.md).

- Comment what the code cannot show on its own. Do not restate the next line.
- Keep the established humanised tone in existing files; do not strip it out.

## Commits

See [.brainbox/guides/COMMIT_STANDARDS.md](.brainbox/guides/COMMIT_STANDARDS.md).

- Format: `<emoji><type>: <subject>`, subject 65 characters or fewer, e.g. `🐞fix: correct vein traversal`.
- Split work into separate logical commits: feature, fix, refactor, build, docs.
- Optimization and runtime commits require in-game validation gates between stages.
- Tell the user explicitly when a commit has been made.

## Before Committing

1. Re-read every file you changed for redundancy, duplication and dead code; extract shared helpers.
2. Run the validators above.
3. Confirm no stale in-progress todo items remain.
