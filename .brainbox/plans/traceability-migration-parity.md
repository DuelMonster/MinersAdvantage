# Parity Traceability Matrix (Current Snapshot)

This matrix tracks modernization parity status against legacy behavior and is updated as milestones land.

Status Legend:
- ✅ Implemented: functional on Fabric and NeoForge for all four nodes
- ⚠️ Partial: implemented in part; specific parity gaps remain
- ❌ Missing: not implemented

Updated: 2026-05-11 (M8 documentation signoff snapshot)

---

## 1. Bootstrap and Lifecycle

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Fabric ModInitializer bootstrap | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantageModInitializer.java | None |
| Fabric client entrypoint wiring | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/FabricClientEntrypoint.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java | None |
| NeoForge @Mod bootstrap | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core/MinersAdvantage.java | None |
| Server tick routing | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | None |
| Core service bootstrap | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/core | None |

## 2. Client Input and Keybindings

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Keybinding metadata catalog | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/KeyBindings.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java | None |
| Keybinding registration (Fabric/NeoForge) | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/ClientInputHandler.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeClientEvents.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java | None |
| Client tick input loop | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/ClientInputHandler.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeClientEvents.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java | None |
| ClientInputService runtime wiring | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/common/services/input/ClientInputService.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client | None |
| Toggle and action dispatch intents | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/common/services/input/ClientInputService.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java | None |

## 3. Server Events and Dispatch

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Block break and tool dispatch | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java, src/main/java/uk/co/duelmonster/minersadvantage/common/event/CommonEventHandlerImpl.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | None |
| Player login/logout hooks | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | None |
| World/level unload cleanup | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | None |
| Entity spawn/load interception | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | None |
| Tool modification hook | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java | Implemented via direct NeoForge event and Fabric interaction-routing equivalent |

## 4. Networking and Packet Transport

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Typed packet records and codecs | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/common/network | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | None |
| C2S registration: component toggle | ✅ | versions/*-fabric/src/main/java/uk/co/duelmonster/minersadvantage/client/FabricNetworkEvents.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeNetworkEvents.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | None |
| C2S registration: abort workers | ✅ | versions/*-fabric/src/main/java/uk/co/duelmonster/minersadvantage/client/FabricNetworkEvents.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeNetworkEvents.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | None |
| C2S registration: player state sync | ✅ | versions/*-fabric/src/main/java/uk/co/duelmonster/minersadvantage/client/FabricNetworkEvents.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeNetworkEvents.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | None |
| FeatureDispatch transport registration | ✅ | versions/*-fabric/src/main/java/uk/co/duelmonster/minersadvantage/client/FabricNetworkEvents.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeNetworkEvents.java, src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | Registered and dispatched on both loaders |
| SupremeVantage transport registration | ✅ | versions/*-fabric/src/main/java/uk/co/duelmonster/minersadvantage/client/FabricNetworkEvents.java, versions/*-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeNetworkEvents.java, src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/network/NetworkHandler.java | Registered and dispatched on both loaders |

## 5. Config UI and Screens

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Fabric ModMenu integration | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/ModMenuEntrypoint.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/events/client | None |
| Shared YACL config screen | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/MinersAdvantageConfigScreen.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/config | None |
| NeoForge config factory registration | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/config | None |
| Config save and sync dispatch | ✅ | src/main/java/uk/co/duelmonster/minersadvantage/client/MinersAdvantageConfigScreen.java | releases/original/src/main/java/uk/co/duelmonster/minersadvantage/config | None |

## 6. Assets and Localization

| Feature | Status | Current Reference | Legacy Reference | Remaining Gaps |
| --- | --- | --- | --- | --- |
| Resource directory structure | ✅ | src/main/resources/assets/minersadvantage | releases/original/src/main/resources/assets/minersadvantage | None |
| English localization file | ✅ | src/main/resources/assets/minersadvantage/lang/en_us.json | releases/original/src/main/resources/assets/minersadvantage/lang/en_us.json | None |
| Russian localization file | ✅ | src/main/resources/assets/minersadvantage/lang/ru_ru.json | releases/original/src/main/resources/assets/minersadvantage/lang/ru_ru.json | None |
| Rewrite keybinding localization keys | ✅ | src/main/resources/assets/minersadvantage/lang/en_us.json, src/main/resources/assets/minersadvantage/lang/ru_ru.json | releases/original/src/main/resources/assets/minersadvantage/lang | None |

---

## M9 Regression Gate Evidence

Validated on 2026-05-11:
- Aggregate build and tests: ./gradlew chiseledBuild (pass)
- NeoForge regression check: ./gradlew :1.21.11-neoforge:test :26.1.2-neoforge:test (pass)
- Full build gate (without tests) had already passed earlier in this sequence via build -x test

## Remaining Work Summary

No outstanding parity blockers remain in this matrix snapshot.
