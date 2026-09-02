# Release Checklist

This checklist is the final handoff flow after staged optimization or feature trains are complete.

## Scope

Use this before publishing jars and creating a release note.

- Applies to Fabric and NeoForge nodes.
- Assumes branch history is already accepted for behavior changes.

## Pre-Release Hygiene

1. Confirm clean working tree:
   - `git status --short`
2. Confirm release branch head and commit messages are finalized.
3. Confirm [CHANGELOG.md](CHANGELOG.md) contains entries for all user-visible changes in this release window.
4. Confirm docs parity across:
   - [README.md](README.md)
   - [TECHNICAL.md](TECHNICAL.md)
   - [TUNING_MATRIX.md](TUNING_MATRIX.md) when throughput or replay policy language changed.

## Validation Gates

Run the same validations enforced by hooks:

1. Version bump validation.
2. Optimization validation.
3. Documentation validation.
4. Full test suite.
5. Full compile matrix (all 6 nodes).
6. Changelog validation.

## In-Game Validation Rule

1. Optimization/runtime commits require in-game validation gates between stages.
2. Docs-only commits are exempt from in-game validation and may proceed once standard validation gates pass.

## Build and Packaging

1. Build all nodes:
   - `./gradlew chiseledBuild`
2. Package release jars:
   - `./gradlew chiseledPackageRelease`
3. Verify output artifacts in [releases](releases).

## Publish Readiness

1. Confirm loader/version pairs are present for intended release:
   - `1.21.11-fabric`
   - `1.21.11-neoforge`
   - `26.1.2-fabric`
   - `26.1.2-neoforge`
   - `26.2-fabric`
   - `26.2-neoforge`
2. Confirm platform metadata (icon/mod metadata) remains valid across loaders.
3. Confirm no known high-severity regressions are open for this release cut.
4. Preview the release notes that will be uploaded (spans every changelog section newer than the version already live on Modrinth):
   - `./gradlew :1.21.11-fabric:printReleaseChangelog`

## Release Record

1. Tag and push release commit according to repository policy.
2. Publish artifacts.
3. Post release summary including:
   - Commit range
   - Validation status
   - Any known issues or follow-up tickets
