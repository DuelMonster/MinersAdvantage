# Miners Advantage

Miners Advantage is being rewritten as a modern, multi-loader project that keeps legacy gameplay behavior while replacing legacy build and architecture foundations.

## Modernization Status

| Phase | Status | Notes |
| --- | --- | --- |
| 0 Repository Reset | complete | Legacy source archived in releases/original and untracked. |
| 1 Build Foundation | complete | Stonecutter + Modstitch scaffold created for four nodes. |
| 2 Architecture | complete | Component lifecycle, descriptor registry, and loader-neutral orchestration are fully wired. |
| 3 Parity Rewrite | **in-progress** | **Current gaps:** Server Events (5/7 event types missing), Networking (input packets live; config sync packet still pending), Config UI (ModMenu/NeoForge factories missing), Assets (localization not migrated). Bootstrap and Client Input are wired across all four nodes. See [Parity Traceability Matrix](.brainbox/plans/traceability-migration-parity.md) for details. Migration plan in progress (Milestones 1-9). |
| 4 Shared Modules | complete | Shared modules finalized, including processing-core, world-query, drop-core, substitution-core, illumination-core, inventory-core, tree-core, farming-core, sync-core, policy-core, and harvest-core. |
| 5 Unit Tests | complete | Deterministic service/runtime/policy/input/packet tests cover parity-critical behavior with final cross-feature policy assertions. |
| 6 Docs/CI/Release | **pending** | Awaiting completion of Phase 3 parity work (Milestones 4-9); final docs/CI/release signoff after verification. |

## Loader and Version Matrix

| Minecraft | Fabric | NeoForge |
| --- | --- | --- |
| 1.21.11 | yes | yes |
| 26.1.x | yes | yes |

## Architecture

The rewrite uses three namespaces:

- common: reusable game logic and services.
- fabric: Fabric-only adapters.
- neoforge: NeoForge-only adapters.

Shared modules introduced in this rewrite:

- processing-core
- world-query
- harvest-core
- drop-core
- illumination-core
- inventory-core
- substitution-core
- tree-core
- farming-core
- sync-core
- policy-core

For deeper implementation details, see TECHNICAL.md.

Final strict traceability mapping is documented in [.brainbox/plans/traceability-final-audit.md](.brainbox/plans/traceability-final-audit.md).

## Build

```bash
./gradlew chiseledBuild
```

Build a single active node by switching Stonecutter active version and running:

```bash
./gradlew build
```

## Release Packaging

```bash
./gradlew chiseledPackageRelease
```

Packaged release jars are staged in releases/.

## License

MIT

## Credits

- Original mod and modernization effort: DuelMonster
- Build stack: Stonecutter, Modstitch, Gradle
