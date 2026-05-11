# Parity Baseline Traceability Matrix

This matrix is the source of truth for all migration work. Each row maps a legacy feature area to its current implementation status with concrete source anchors.

**Status Legend:**
- ✅ **Implemented** — Fully functional on both Fabric and NeoForge
- ⚠️ **Partial** — Partially implemented; known gaps listed
- ❌ **Missing** — Not implemented; requires full implementation
- 🔴 **Regressed** — Was working, now broken; requires restoration

**Updated:** 2026-05-11 (Milestone 1 baseline)

---

## 1. Bootstrap & Lifecycle

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Fabric ModInitializer** | ✅ Implemented | [ModEntry.java:26-84](../../../src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java#L26-L84) | [releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantageModInitializer.java](../../../releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantageModInitializer.java) | None; initialization flow intact. |
| **Fabric client entrypoint** | ❌ Missing | Does not exist | (Forge had client event bus; no exact equivalent) | No FabricClientEntrypoint class; keybinding/client-tick registration has no entry point. **Blocks M3, M4, M6.** |
| **NeoForge @Mod bootstrap** | ❌ Missing (commented) | [ModEntry.java:85-147](../../../src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java#L85-L147) (all commented) | [releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantage.java:1-80](../../../releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantage.java#L1-L80) | Entire NeoForge init section commented; @Mod constructor and event bus registration missing. **CRITICAL: Blocks all NeoForge initialization.** |
| **Server tick routing** | ⚠️ Partial | [ModEntry.java:40-59](../../../src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java#L40-L59) (Fabric only) | [releases/original/events/server/ServerEventHandler.java:18-28](../../../releases/original/events/server/ServerEventHandler.java#L18-L28) | NeoForge side commented out; no common routing point; unclear if both loaders call core.serverTick() consistently. |
| **Core service bootstrap** | ✅ Implemented | [MinersAdvantageCore.java:60-100](../../../src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java#L60-L100) | (Original had individual feature init) | MinersAdvantageCore.bootstrap() called once per loader, all 11 components registered. |

---

## 2. Client Input & Keybindings

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Keybinding metadata** | ✅ Implemented | [KeyBindings.java:6-40](../../../src/main/java/uk/co/duelmonster/minersadvantage/client/KeyBindings.java#L6-L40) | [releases/original/events/client/KeyInputEvents.java:11-30](../../../releases/original/events/client/KeyInputEvents.java#L11-L30) | All 14 keybinding specs defined with defaults (KP_1 through KP_8, DELETE, GRAVE, TAB, LEFT_ALT, V, F12). |
| **Keybinding registration** | ❌ Missing | Does not exist | [releases/original/events/client/ClientEventHandler.java (RegisterKeyBindingsEvent listener)](../../../releases/original/events/client/ClientEventHandler.java) | No KeyMapping instances created, no registration listener, no consumeClick() loop. **Blocks M3, causes player input to fail.** |
| **Client tick input loop** | ❌ Missing | Does not exist | [releases/original/events/client/KeyInputEvents.java:40-130](../../../releases/original/events/client/KeyInputEvents.java#L40-L130) | No ClientTickEvents listener consuming key presses and invoking ClientInputService.process(). **User input completely unwired.** |
| **ClientInputService wiring** | ❌ Missing | [ClientInputService.java:exists, 160+ lines](../../../src/main/java/uk/co/duelmonster/minersadvantage/common/services/input/ClientInputService.java) (NOT CALLED) | [releases/original/client-side input state machine logic (distributed)](../../../releases/original/events/client/) | Service fully implemented but never invoked from any event listener. Output packets not sent. |
| **Feature toggle actions** | ❌ Missing (unwired) | [KeyBindings.java:6-22 (ClientAction enum)](../../../src/main/java/uk/co/duelmonster/minersadvantage/client/KeyBindings.java#L6-L22) | [releases/original/events/client/KeyInputEvents.java:actions](../../../releases/original/events/client/KeyInputEvents.java) | All 14 actions defined (CAPTIVATION_TOGGLE, EXCAVATION_TOGGLE, etc.) but no dispatch to feature toggles. |

---

## 3. Server Events & Feature Dispatch

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Block break event** | ✅ Implemented | [ModEntry.java:50-56](../../../src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java#L50-L56) | [releases/original/events/server/ServerEventHandler.java:122-180](../../../releases/original/events/server/ServerEventHandler.java#L122-L180) | PlayerBlockBreakEvents hooked, calls toolEvents handlers. |
| **Tool use dispatch** | ✅ Implemented | [ModEntry.java:28-32, 50-56](../../../src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java#L28-L32) | [releases/original/events/server/ServerEventHandler.java:122-180](../../../releases/original/events/server/ServerEventHandler.java#L122-L180) | routeToolUse() exists and dispatches to feature handlers. |
| **Player login event** | ❌ Missing | Does not exist | [releases/original/events/server/ServerEventHandler.java:36-50](../../../releases/original/events/server/ServerEventHandler.java#L36-L50) | No ServerPlayConnectionEvents.JOIN hook; state sync packet not sent on join. **Blocks player state synchronization.** |
| **Player logout event** | ❌ Missing | Does not exist | [releases/original/events/server/ServerEventHandler.java:52-66](../../../releases/original/events/server/ServerEventHandler.java#L52-L66) | No ServerPlayConnectionEvents.DISCONNECT hook; cleanup not called. |
| **World/level unload event** | ❌ Missing | Does not exist | [releases/original/events/server/ServerEventHandler.java:68-82](../../../releases/original/events/server/ServerEventHandler.java#L68-L82) | No LevelEvents.UNLOAD handler; agents not cleared on unload. **Blocks state cleanup.** |
| **Entity/item spawn interception** | ❌ Missing | Does not exist | [releases/original/events/server/ServerEventHandler.java:230-280](../../../releases/original/events/server/ServerEventHandler.java#L230-L280) | No ItemEntity or ExperienceOrb spawn listeners; drops not intercepted. **Blocks drop capture.** |
| **Tool modification (hoe/shovel)** | ❌ Missing | Does not exist | [releases/original/events/server/ServerEventHandler.java:290-310](../../../releases/original/events/server/ServerEventHandler.java#L290-L310) | No BlockToolModificationEvent listener; hoe/shovel actions not routed. **Blocks Cultivation, Farming behavior.** |

---

## 4. Networking & Packet Transport

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Packet record types** | ✅ Implemented | [common/network/*.java (6 records)](../../../src/main/java/uk/co/duelmonster/minersadvantage/common/network/) | [releases/original/network/NetworkHandler.java:50-100](../../../releases/original/network/NetworkHandler.java#L50-L100) | All 6 payload types defined (FeatureDispatchPacket, PlayerStateSyncPacket, ComponentTogglePacket, AbortWorkersPacket, SupremeVantagePacket, + one more). |
| **Fabric payload registration** | ❌ Missing | PacketRegistry.java only has static ID constants | [releases/original/network/NetworkHandler.java:SimpleChannel setup](../../../releases/original/network/NetworkHandler.java) | No StreamCodec implementations, no PayloadTypeRegistry.register() calls. **Blocks packet transport on Fabric.** |
| **NeoForge payload registration** | ❌ Missing | PacketRegistry.java only has static ID constants | (Forge had SimpleChannel with IMessageHandler) | No IPayloadHandler implementations, no NetworkDirection registration. **Blocks packet transport on NeoForge.** |
| **Packet handler dispatch** | ❌ Missing (stubs exist) | [MinersAdvantageCore.java:handleComponentTogglePacket, handlePlayerStateSyncPacket, etc.](../../../src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java) (methods exist but never called) | [releases/original/network/NetworkHandler.java:150-200](../../../releases/original/network/NetworkHandler.java#L150-L200) | Handler stubs defined in core but packet handlers never registered or invoked. **Blocks server-side packet processing.** |
| **Packet send sites** | ❌ Missing (unwired) | ClientInputService.process() returns packet list | [releases/original/events/client/KeyInputEvents.java:80-120](../../../releases/original/events/client/KeyInputEvents.java#L80-L120) | No ClientPlayNetworking.send() (Fabric) or SimpleChannel.sendToServer() (NeoForge) calls from input loop. **Blocks client→server communication.** |
| **S2C sync packets** | ❌ Missing (unwired) | PlayerStateSyncPacket record exists | [releases/original/network/NetworkHandler.java S2C handler](../../../releases/original/network/NetworkHandler.java) | No ServerPlayNetworking.send() calls on player login/config change; client config not synced. **Blocks config propagation.** |

---

## 5. Config UI & Screens

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Fabric ModMenu integration** | ❌ Missing (stubbed) | [ModMenuEntrypoint.java:10](../../../src/main/java/uk/co/duelmonster/minersadvantage/client/ModMenuEntrypoint.java#L10) (returns `parent -> null`) | N/A (Forge did not use ModMenu; was InGame Config) | Config button declared in fabric.mod.json but factory returns null. **Config UI inaccessible on Fabric.** |
| **YACL ConfigScreen builder** | ❌ Missing | Does not exist | (Conceptual; Forge had InGame Config) | No ConfigScreen class; no YACL categories for features. **Blocks config UI on both loaders.** |
| **NeoForge IConfigScreenFactory** | ❌ Missing | Does not exist | [Conceptual; NeoForge standard pattern](https://docs.neoforged.net/neo/1.20.1/docs/gettingstarted/netty/) | No ConfigScreenFactoryBridge; extension point not registered. **Blocks config UI on NeoForge.** |
| **Config persistence** | ⚠️ Partial | SyncedClientConfig record exists | (Original had file-based config) | Config model defined but no save/load lifecycle wired to screens. |
| **Localization keys for config** | ❌ Missing | No en_us.json, ru_ru.json | [releases/original/src/main/resources/assets/minersadvantage/lang/en_us.json](../../../releases/original/src/main/resources/assets/minersadvantage/lang/en_us.json) | No localization strings for config labels, keybinding names. **Blocks i18n for UI.** |

---

## 6. Assets & Localization

| Feature | Status | Current Reference | Legacy Reference | Gaps & Blockers |
|---------|--------|-------------------|------------------|-----------------|
| **Resource directory structure** | ❌ Missing | src/main/resources/ does not exist | [releases/original/src/main/resources/assets/minersadvantage/](../../../releases/original/src/main/resources/assets/minersadvantage/) | Directory structure not created. **Blocks all asset loading.** |
| **Language files (en_us.json)** | ❌ Missing | Not migrated | [releases/original/src/main/resources/assets/minersadvantage/lang/en_us.json](../../../releases/original/src/main/resources/assets/minersadvantage/lang/en_us.json) | All English translations lost. **Blocks English localization.** |
| **Language files (ru_ru.json)** | ❌ Missing | Not migrated | [releases/original/src/main/resources/assets/minersadvantage/lang/ru_ru.json](../../../releases/original/src/main/resources/assets/minersadvantage/lang/ru_ru.json) | All Russian translations lost. **Blocks Russian localization.** |
| **Mixin metadata** | ✅ Implemented | [minersadvantage.mixins.json (template)](../../../src/main/templates/minersadvantage.mixins.json) | [releases/original/src/main/resources/minersadvantage.mixins.json](../../../releases/original/src/main/resources/minersadvantage.mixins.json) | Metadata defined; tag alignment needs verification. |
| **Manifest localization keys** | ⚠️ Partial | fabric.mod.json: "name": "Miners Advantage" (hardcoded) | (Legacy used i18n keys) | No i18n keys used in manifests; should use localization for display name if desired. |

---

## Completion Checklist

Exit criteria for Milestone 1 baseline:

- [ ] Matrix reviewed by user; all 6 areas have status assigned
- [ ] No rows remain unassigned or TBD
- [ ] Source anchors verified (current + legacy refs point to real files)
- [ ] CHANGELOG.md updated with baseline docs change
- [ ] README.md and TECHNICAL.md status updated to reflect "In Progress" migration
- [ ] Version bumped to 0.3.0 (first change day only)
- [ ] No blocking issues identified that would prevent M2-M9 execution

---

## Migration Workflow

This matrix drives all downstream milestones:

1. **Milestone 1** (current) — Establish baseline traceability
2. **Milestone 2** — Work all ❌ and 🔴 items in Bootstrap section (status → ✅)
3. **Milestone 3** — Work all ❌ and 🔴 items in Client Input section (status → ✅)
4. **Milestone 4** — Work all ❌ and 🔴 items in Networking section (status → ✅)
5. **Milestone 5** — Work all ❌ and 🔴 items in Server Events section (status → ✅)
6. **Milestone 6** — Work all ❌ and 🔴 items in Config UI section (status → ✅)
7. **Milestone 7** — Work all ❌ and 🔴 items in Assets section (status → ✅)
8. **Milestone 8** — Update this matrix, README, TECHNICAL, CHANGELOG with final status
9. **Milestone 9** — Verify all rows are ✅; no ⚠️ or ❌ remain

