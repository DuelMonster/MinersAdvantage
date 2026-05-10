# TECHNICAL

## Overview

This repository is a source-driven rewrite of Miners Advantage with a hard reset policy:

- no legacy migration code
- no compatibility adapters for old config formats
- shared reusable logic extracted into common services

## Build System

### Core stack

- Gradle Kotlin DSL
- Stonecutter for node slicing
- Modstitch for unified Fabric and NeoForge wiring

### Nodes

| Node | Minecraft | Loader | Properties file |
| --- | --- | --- | --- |
| 1.21.11-fabric | 1.21.11 | Fabric | versions/1.21.11-fabric/gradle.properties |
| 1.21.11-neoforge | 1.21.11 | NeoForge | versions/1.21.11-neoforge/gradle.properties |
| 26.1.2-fabric | 26.1.2 | Fabric | versions/26.1.2-fabric/gradle.properties |
| 26.1.2-neoforge | 26.1.2 | NeoForge | versions/26.1.2-neoforge/gradle.properties |

### Aggregate tasks

- chiseledBuild
- chiseledPublishAll
- chiseledPackageRelease

## Code Architecture

### Package split

- uk.co.duelmonster.minersadvantage.common
- uk.co.duelmonster.minersadvantage.fabric
- uk.co.duelmonster.minersadvantage.neoforge

### Lifecycle contract

All components implement:

- register
- enable
- disable
- tick
- cleanup

Descriptor-driven registration is handled by ComponentRegistry.

### Shared modules and ownership

| Module | Class | Responsibility |
| --- | --- | --- |
| processing-core | ProcessingCoreService | queue limits, per-tick budget, cancellation through clear |
| world-query | WorldQueryService | geometry bounds and area helpers |
| harvest-core | HarvestCoreService | maturity and durability cadence helpers |
| drop-core | DropCoreService | capture and flush semantics for drops |
| illumination-core | IlluminationCoreService | torch placement decision support |
| inventory-core | InventoryCoreService | inventory presence and consumption |
| substitution-core | SubstitutionCoreService | tool ranking and filtering |
| tree-core | TreeCoreService | trunk/leaf heuristics |
| farming-core | FarmingCoreService | hydration proximity checks |
| cropination-core | CropinationCoreService | crop maturity, replant, and durability action decisions |
| lumbination-core | LumbinationCoreService | trunk/leaf traversal planning and sapling replant intent |
| captivation-core | CaptivationCoreService | item capture eligibility, GUI gate, and radius policy |
| sync-core | SyncCoreService | per-player state snapshots |
| policy-core | PolicyCoreService | config range clamping and server override gates |

## Feature descriptors

The core bootstrap registers these components:

- CAPTIVATION
- CROPINATION
- CULTIVATION
- EXCAVATION
- PATHANATION
- ILLUMINATION
- LUMBINATION
- SHAFTANATION
- SUBSTITUTION
- VEINATION
- VENTILATION

## Unit Testing Strategy

Tests are unit-only and currently target deterministic services:

- queue boundaries and tick budgets
- geometry helpers
- substitution ranking preference behavior
- illumination placement decisions
- policy clamping and overrides
- drop capture/flush semantics
- component bootstrap coverage
- farming/harvest/captivation runtime planning and decision outputs

## CI and Release

- CI workflow builds all nodes through chiseledBuild
- Release workflow builds, stages jars, and publishes artifacts

## License

MIT

## Credits

- DuelMonster
- Stonecutter, Modstitch, Gradle contributors
