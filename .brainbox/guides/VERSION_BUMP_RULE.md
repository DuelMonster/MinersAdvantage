# Version Bump Rule

This guide defines the version bump policy used in this repository.

## Hard Rule

1. A version bump may happen at most once per calendar day.
2. The first version bump of a day must be accompanied by an update to the tracked last-bumped date file.
3. If the tracked last-bumped date already matches today, a second bump is blocked unless the user has explicitly requested an override.
4. If the user asks for a different version change, follow the user's explicit instruction, but only through the documented override path.

## Tracked State

The repository stores the last bump date in [.brainbox/state/version-bump-state.txt](../state/version-bump-state.txt).

The file must contain the most recent successful bump date in ISO format, for example:

```text
Last Bumped Date: 2026-05-12
```

The pre-commit validator checks this file whenever `gradle.properties` changes its `mod_version` value.

## Enforcement Checklist

Before creating a commit that changes `mod_version`:

1. Read the staged `mod_version` value from `gradle.properties`.
2. Read the staged last-bumped date from [.brainbox/state/version-bump-state.txt](../state/version-bump-state.txt).
3. Confirm the staged date matches today.
4. Confirm the previously committed date does not already match today.
5. If an override is required, stage [.brainbox/state/version-bump-override.txt](../state/version-bump-override.txt) with today's date and a reason, then proceed only because the user explicitly requested it.

After a successful bump commit:

1. Keep the last-bumped date file at today's date.
2. Do not perform another bump on the same day unless the override path is used deliberately.

## Enforcement Path

The pre-commit hook runs a version-bump validator before the commit is accepted. That validator blocks commits when:

- `mod_version` changes without a matching update to the last-bumped date file
- the staged last-bumped date is not today
- the previously committed last-bumped date is already today and no override is staged

This makes the rule machine-enforced instead of "please remember not to do that again", which is how bugs get to laugh at us.

## Scope

This rule applies to all regular feature, fix, refactor, docs, and maintenance work in this repository.
