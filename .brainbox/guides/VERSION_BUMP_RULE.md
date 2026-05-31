# Version Bump Rule

This guide defines the version bump policy used in this repository.

## Hard Rule

1. A version bump may happen at most once per calendar day.
2. The first version bump of a day must be accompanied by an update to the tracked last-bumped date file.
3. If the tracked last-bumped date already matches today, a second bump is blocked unless the user has explicitly requested an override.
4. If the user asks for a different version change, follow the user's explicit instruction, but only through the documented override path.

## Tracked State

The repository stores bump metadata in [.brainbox/state/version-bump-state.txt](../state/version-bump-state.txt).

The file must contain both the most recent successful bump date and the next expected version, for example:

```text
Last Bumped Date: 2026-05-12
Next Version: 2.9.0
```

The pre-commit validator checks this file for every commit.

## Enforcement Checklist

Before creating a commit:

1. Read the effective state content from [.brainbox/state/version-bump-state.txt](../state/version-bump-state.txt) (staged version if present, otherwise HEAD).
2. Confirm `Last Bumped Date` matches today.
3. Confirm `Next Version` exists and is valid SemVer (`X.Y.Z`).
4. If today differs from the previously committed date, the commit must bump `mod_version` and the bumped value must match the previously committed `Next Version` (or be greater than HEAD when migrating from old state format).
5. Whenever `mod_version` is bumped, update `Next Version` so it is greater than the bumped version.
6. If a second bump is required on the same day, stage [.brainbox/state/version-bump-override.txt](../state/version-bump-override.txt) with today's date and a reason, and proceed only because the user explicitly requested it.

After a successful bump commit:

1. Keep the last-bumped date file at today's date.
2. Advance `Next Version` so it remains ahead of the committed `mod_version`.
3. Do not perform another bump on the same day unless the override path is used deliberately.

## Enforcement Path

The pre-commit hook runs a version-bump validator before the commit is accepted. That validator blocks commits when:

- the effective `Last Bumped Date` is not today
- the state file does not contain a valid `Next Version`
- a new-day commit does not perform a real `mod_version` bump
- a bumped `mod_version` does not match the expected `Next Version`
- bumped `mod_version` is not accompanied by an advanced `Next Version`
- a second same-day bump is attempted without a valid override

This makes the rule machine-enforced instead of "please remember not to do that again", which is how bugs get to laugh at us.

## Scope

This rule applies to all regular feature, fix, refactor, docs, and maintenance work in this repository.
