# 📚 Documentation Standards Guide

This guide defines the standards for all end-user and technical documentation in this repository.
Every commit that touches code, features, or configuration **must** keep documentation consistent
with these standards. The pre-commit hook enforces structural requirements automatically.

---

## Overview

The repository maintains two primary documentation files:

| File           | Audience        | Tone                                    |
| -------------- | --------------- | --------------------------------------- |
| `README.md`    | End users       | Friendly, humanised, mildly comedic     |
| `TECHNICAL.md` | Developers      | Clear, precise, technically complete    |

Both files must always reflect the current state of the mod. "We'll update it later" is not a
valid workflow — update docs alongside the code change, before committing.

---

## README.md Standards

### Tone & Voice

The README is the mod's shop window. It should be **friendly, humanised, and aimed squarely at
players** who may not be developers. A light comedic flair is encouraged — dry wit, self-aware
jokes about Minecraft quirks, and playful section intros all belong here. It should never feel
like a dry feature list or a corporate manual.

Rules:
- Write as if explaining the mod to a friend who plays Minecraft
- Use second person ("you", "your") to feel direct and personal
- Keep it accessible — avoid heavy jargon without explanation
- Sprinkle in personality without sacrificing clarity

### Required Sections

The following sections **must** be present in this order:

| Section                   | Heading (exact)              | Description                                                    |
| ------------------------- | ---------------------------- | -------------------------------------------------------------- |
| Overview                  | `## Overview`                | Short, punchy summary of what the mod does and why it exists   |
| Features                  | `## Features`                | Bullet list of top-level features                              |
| How It Works              | `## How It Works`            | Per-feature explanation of mechanics in plain language         |
| Installation              | `## Installation`            | Step-by-step install for **both** Fabric and NeoForge          |
| Configuration             | `## Configuration`           | All config options, formats, defaults, and effects             |
| Compatibility             | `## Compatibility`           | Known compatible/incompatible mods and versions                |
| Technical Documentation   | `## Technical Documentation` | Short note linking to `TECHNICAL.md`                           |
| License                   | `## License`                 | MIT licence statement                                          |
| Credits                   | `## Credits`                 | Acknowledgements, contributors, libraries used                 |

### Section Content Guidelines

#### Overview
One to three paragraphs. Lead with *what problem the mod solves* and *why a player would care*.
The tone should grab attention immediately.

#### Features
A clean bullet list. Each item is a single sentence describing a feature by name. No deep
mechanics here — that's for "How It Works".

#### How It Works
One sub-section (or at minimum a paragraph) per feature listed under Features. Explain the
*behaviour* a player will observe and any relevant interactions. Keep language plain — assume
the reader knows Minecraft but not your codebase.

#### Installation
Must cover **both loaders**. Use a subsection or a clear label for each:
- Fabric installation steps (includes Fabric API requirement if applicable)
- NeoForge installation steps

Steps should be numbered and actionable.

#### Configuration
Document **every** config option. Use a table where there are multiple options:

```markdown
| Option   | Type    | Default | Description                  |
| -------- | ------- | ------- | ---------------------------- |
| foo_bar  | boolean | true    | Enables the foo-bar feature  |
```

All table columns must be space-padded for raw-text readability (see Formatting section).

#### Compatibility
List known compatible mods, versions supported, and any known conflicts. If there are no known
conflicts, say so explicitly rather than omitting the section.

#### Technical Documentation
A brief sentence and a Markdown link:

```markdown
For full technical details, see [TECHNICAL.md](TECHNICAL.md).
```

#### License
Must explicitly state MIT. Example:

```markdown
MIT — see [LICENSE.md](LICENSE.md) for the full text.
```

#### Credits
Name contributors, key libraries, and any inspirations. Keep it gracious.

---

## TECHNICAL.md Standards

### Tone & Voice

The technical doc is written for developers — people building, contributing to, or auditing the
mod. Precision matters more than personality here. Be clear, be thorough, be accurate. Technical
jargon is fine; vague hand-waving is not.

### Required Sections

| Section              | Heading (exact)         | Description                                                      |
| -------------------- | ----------------------- | ---------------------------------------------------------------- |
| Build Instructions   | `## Build Instructions` | How to build the mod from source using Gradle                    |
| License              | `## License`            | MIT licence statement                                            |
| Credits              | `## Credits`            | Libraries, tools, contributors                                   |

Beyond these three required sections, TECHNICAL.md should be a **complete technical write-up**
covering all significant aspects of the mod — architecture, systems, data flows, extension
points, and anything non-obvious about the implementation. When code changes affect architecture,
update this doc in the same commit.

### Build Instructions Content

Must include:

1. Prerequisites (Java version, Gradle version if pinned, any special toolchain notes)
2. How to run a development client: `./gradlew :<version>-<loader>:runClient`
3. How to run tests: `./gradlew test`
4. How to produce release JARs
5. The Stonecutter multi-version setup (version directories, how to target a specific version)

### License Statement

Must explicitly state MIT. Same requirement as README.

---

## Formatting Standards

### Tables

All Markdown tables must be **space-padded** for raw-text readability. Columns should be aligned
by padding cell content with spaces so that `|` characters form clean vertical lines when viewed
in a plain-text editor.

#### ❌ Unpadded (avoid)
```markdown
| Option | Type | Default | Description |
|---|---|---|---|
| speed | int | 5 | Movement speed |
```

#### ✅ Padded (required)
```markdown
| Option | Type | Default | Description     |
| ------ | ---- | ------- | --------------- |
| speed  | int  | 5       | Movement speed  |
```

### Headings

- Use `##` for top-level sections, `###` for sub-sections within a section
- Do not skip heading levels
- Heading text must match the **exact** strings listed in the Required Sections tables above —
  the pre-commit validator checks for these literally

### Links

- All cross-references to other files must use Markdown links: `[TECHNICAL.md](TECHNICAL.md)`
- Do not use bare paths or raw URLs for files within the repository

### Markdown Hygiene

- One blank line before and after headings
- One blank line between paragraphs
- Fenced code blocks with an explicit language hint (` ```markdown `, ` ```bash `, etc.)

---

## Validation

The pre-commit hook runs `scripts/validate-docs.ps1` (Windows) or `scripts/validate-docs.sh`
(Unix/macOS) before every commit. Validation fails and the commit is blocked if:

- `README.md` or `TECHNICAL.md` is missing
- Any required section heading is absent from the relevant file
- `README.md` does not contain a Markdown link to `TECHNICAL.md`
- Either file does not explicitly state the MIT licence
- The README Installation section does not mention both Fabric and NeoForge

### Running Validation Manually

**PowerShell:**
```powershell
.\scripts\validate-docs.ps1
```

**Bash:**
```bash
./scripts/validate-docs.sh
```

A passing run exits with code `0` and prints:
```
Documentation validation passed.
```

A failing run exits with code `1`, lists each failing check, and reminds you which rule file
governs documentation standards.

### Setting Up Hooks

If you have just cloned the repository, run the hook setup script once:

**PowerShell:**
```powershell
.\scripts\setup-hooks.ps1
```

**Bash:**
```bash
./scripts/setup-hooks.sh
```

This configures Git to use `.githooks/` as the hooks directory.

---

## Quick Checklist Before Committing

- [ ] Added or updated content for every changed feature
- [ ] All required README sections present with exact headings
- [ ] Installation section covers both Fabric and NeoForge
- [ ] Configuration table is space-padded and complete
- [ ] All required TECHNICAL sections present with exact headings
- [ ] Build instructions reflect any toolchain changes
- [ ] MIT licence stated in both files
- [ ] `README.md` links to `TECHNICAL.md` via Markdown link
- [ ] Ran `validate-docs` locally and it passes
