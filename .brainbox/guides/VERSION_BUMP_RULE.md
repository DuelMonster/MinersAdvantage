# Version Bump Rule

This guide defines the version bump policy used in this repository.

## Hard Rule

1. On the first code or documentation change day only, bump the minor version by exactly +1 before the first commit of that day, unless explicitly instructed otherwise by the user.
2. Do not bump by more than +1 in a single day.
3. Do not perform extra bumps because multiple days have passed.
4. If the user asks for a different version change, follow the user's explicit instruction.

## Enforcement Checklist

Before creating the first commit of a change day:

1. Confirm whether a version bump has already been applied that day.
2. If not, apply exactly one minor bump.
3. Continue with normal commit flow.

After that first commit:

1. Do not apply additional version bumps unless explicitly requested by the user.

## Scope

This rule applies to all regular feature, fix, refactor, docs, and maintenance work in this repository.
