# MinersAdvantage Parity Checklist (Legacy → Workspace)

## Legend
- [x] Migrated/Present
- [ ] Missing/To migrate
- [~] Partially migrated

---

## 1. Core Java Packages

### client/
- [~] ClientFunctions.java
- [x] KeyBindings.java
- [~] MAParticleManager.java
- [x] ClientInputHandler.java (new)
- [x] FabricClientEntrypoint.java (new)
- [x] MinersAdvantageConfigScreen.java (new)
- [x] ModMenuEntrypoint.java (new)

### common/
- [~] Constants.java
- [~] Functions.java
- [~] JsonHelper.java
- [x] RankAndLevel.java
- [x] Ranking.java
- [x] ReferenceWrapper.java
- [x] SyncType.java
- [x] TorchPlacement.java (migrated as service)
- [~] Variables.java

### config/
- [x] MAConfig.java
- [x] MAConfig_Base.java
- [x] MAConfig_Client.java
- [x] MAConfig_Server.java
- [x] SyncedClientConfig.java
- [x] All feature configs (e.g., CaptivationConfig.java, CropinationConfig.java, etc.)
- [~] categories/ (legacy category class names mirrored)
- [x] defaults/MAConfig_Defaults.java

### events/
- [~] client/ClientEventHandler.java
- [~] client/KeyInputEvents.java
- [~] server/ServerEventHandler.java

### helpers/
- [~] FarmingHelper.java
- [~] IlluminationHelper.java
- [~] LumbinationHelper.java
- [~] ServerInfo.java
- [~] SubstitutionHelper.java
- [~] SupremeVantage.java
- [~] VentilationHelper.java

### network/
- [~] NetworkHandler.java
- [x] packetids/IPacketId.java
- [x] packetids/PacketId.java
- [~] packets/* (legacy packet class names mirrored; no-op process handlers replaced with feature dispatch bridge, remaining runtime parity still incomplete)

### setup/
- [~] ClientSetup.java
- [~] CommonSetup.java
- [~] ServerSetup.java

### utils/
- [~] UtilsCommon.java
- [~] UtilsServer.java

### workers/
- [~] Core compatibility worker classes added (Agent.java, AgentProcessor.java, DropsSpawner.java)
- [~] Feature-specific worker class names mirrored (e.g., ExcavationAgent.java, LumbinationAgent.java, etc.)

---

## 2. Resources
- [x] assets/minersadvantage/lang/en_us.json
- [x] assets/minersadvantage/lang/ru_ru.json
- [x] assets/minersadvantage/lang/SS_JSON.xml
- [x] MinersAdvantage_Banner.png
- [x] MinersAdvantage_Logo.png
- [x] MinersAdvantage_Logo_And_Text.png
- [x] META-INF/accesstransformer.cfg
- [x] META-INF/mods.toml
- [x] pack.mcmeta

---

## 3. Reference Implementations
- [ ] For each missing feature, check Forget-Me-Crops for a modern implementation.

---

## 4. Documentation
- [x] README.md
- [x] LICENSE.md
- [x] CurseForge.md
- [x] changelog.txt (legacy)

---

## 5. Build System
- [x] build.gradle(.kts)
- [x] gradle.properties
- [x] settings.gradle(.kts)
- [~] Validate all legacy gradle scripts for parity (stonecutter/createMinecraftArtifacts task-order dependency patched)

---

## 6. Governance
- [x] All .brainbox/guides/*
- [x] All .brainbox/rules/*

---

## 7. Next Steps
- For each [ ] or [~] entry, reference Forget-Me-Crops and official docs for implementation.
- Update this checklist as migration proceeds.
- Prioritize bridging migrated core/service logic into event and network flows (especially legacy packet/setup/event parity).
- Continue deep parity passes for helper domains still marked [~] where behavior is currently simplified.
