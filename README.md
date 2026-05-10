# Miners Advantage

Miners Advantage is being rewritten as a modern, multi-loader project that keeps legacy gameplay behavior while replacing legacy build and architecture foundations.

## Modernization Status

| Phase | Status | Notes |
| --- | --- | --- |
| 0 Repository Reset | complete | Legacy source archived in releases/original and untracked. |
| 1 Build Foundation | complete | Stonecutter + Modstitch scaffold created for four nodes. |
| 2 Architecture | in progress | Component lifecycle and descriptor registry implemented. |
| 3 Parity Rewrite | in progress | Feature map and service boundaries implemented; gameplay parity logic is being filled feature by feature. |
| 4 Shared Modules | in progress | processing-core, world-query, drop-core, substitution-core, illumination-core, inventory-core, tree-core, farming-core, sync-core, policy-core, harvest-core created. |
| 5 Unit Tests | in progress | Core deterministic service tests added. |
| 6 Docs/CI/Release | in progress | CI and release workflows added; documentation under active expansion. |

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
