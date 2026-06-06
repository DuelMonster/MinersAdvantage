package uk.co.duelmonster.minersadvantage.common;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import uk.co.duelmonster.minersadvantage.agent.AgentManager;
import uk.co.duelmonster.minersadvantage.agent.IlluminationAgent;
import uk.co.duelmonster.minersadvantage.agent.IlluminationPlaceAgent;
import uk.co.duelmonster.minersadvantage.common.component.ComponentDescriptor;
import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentRegistry;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.feature.captivation.CaptivationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.farming.CropinationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.farming.CultivationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.harvest.LumbinationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.ExcavationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.ShaftanationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.mining.VentilationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.IlluminationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.PathanationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.SubstitutionComponent;
import uk.co.duelmonster.minersadvantage.common.feature.utility.VeinationComponent;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.IlluminationActionPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;
import uk.co.duelmonster.minersadvantage.common.event.FeatureEventHandler;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.core.ServerTickOrchestrator;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;
import uk.co.duelmonster.minersadvantage.common.services.sync.SyncCoreService;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * MinersAdvantageCore keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class MinersAdvantageCore {
    private final ComponentRegistry componentRegistry = new ComponentRegistry();
    private final Map<FeatureId, ComponentLifecycle> components = new EnumMap<>(FeatureId.class);
    private final PlayerStateService playerStateService;
    private final ServerTickOrchestrator tickOrchestrator;
    private final PolicyCoreService policyCoreService;
    private final SyncCoreService syncCoreService;
    private final SupremeVantageService supremeVantageService;
    private final SyncedClientConfig defaultConfig;

    /**
     * MinersAdvantageCore exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public MinersAdvantageCore() {
        this.playerStateService = new PlayerStateService();
        this.tickOrchestrator = new ServerTickOrchestrator(playerStateService);
        this.policyCoreService = new PolicyCoreService();
        this.syncCoreService = new SyncCoreService();
        this.supremeVantageService = new SupremeVantageService();
        this.defaultConfig = MAConfig_Base.getGlobalConfig();
    }

    /**
     * bootstrap exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void bootstrap() {
        LogUtils.logInfo("Bootstrapping core components");
        MAShapeBootstrap.ensureInitialized();
        tickOrchestrator.setTpsGuardActive(defaultConfig.common().tpsGuard());
        tickOrchestrator.setProcessingDelay(defaultConfig.common().enableTickDelay(), defaultConfig.common().tickDelay());
        registerFeature(FeatureId.CAPTIVATION, "captivation", defaultConfig::captivation, CaptivationComponent::new);
        registerFeature(FeatureId.CROPINATION, "cropination", defaultConfig::cropination, CropinationComponent::new);
        registerFeature(FeatureId.CULTIVATION, "cultivation", defaultConfig::cultivation, CultivationComponent::new);
        registerFeature(FeatureId.EXCAVATION, "excavation", defaultConfig::excavation, ExcavationComponent::new);
        registerFeature(FeatureId.ILLUMINATION, "illumination", defaultConfig::illumination, IlluminationComponent::new);
        registerFeature(FeatureId.LUMBINATION, "lumbination", defaultConfig::lumbination, LumbinationComponent::new);
        registerFeature(FeatureId.PATHANATION, "pathanation", defaultConfig::pathanation, PathanationComponent::new);
        registerFeature(FeatureId.SHAFTANATION, "shaftanation", defaultConfig::shaftanation, ShaftanationComponent::new);
        registerFeature(FeatureId.SUBSTITUTION, "substitution", defaultConfig::substitution, SubstitutionComponent::new);
        registerFeature(FeatureId.VEINATION, "veination", defaultConfig::veination, VeinationComponent::new);
        registerFeature(FeatureId.VENTILATION, "ventilation", defaultConfig::ventilation, VentilationComponent::new);
        componentRegistry.enableAll();
        LogUtils.logInfo("Bootstrapped {} feature components", components.size());
    }

    /**
     * r eg is te rf ea tu re exists so this path stays predictable and easier to debug when things get weird.
     */
    private <C> void registerFeature(FeatureId id, String key, Supplier<C> configGetter, Function<C, ? extends ComponentLifecycle> componentFactory) {
        ComponentLifecycle component = componentFactory.apply(configGetter.get());
        components.put(id, component);
        componentRegistry.register(new ComponentDescriptor(key, key.substring(0, 1).toUpperCase() + key.substring(1), component));
        LogUtils.logDebug("Registered component feature={} type={}", id, component.getClass().getSimpleName());
    }

    /**
     * shutdown exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void shutdown() {
        componentRegistry.disableAll();
        componentRegistry.cleanupAll();
    }

    /**
     * components exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public Map<FeatureId, ComponentLifecycle> components() {
        return Map.copyOf(components);
    }

    /**
     * serverTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void serverTick() {
        tickOrchestrator.onServerTick();
        componentRegistry.tickAll();
    }

    /**
     * playerStateService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PlayerStateService playerStateService() {
        return playerStateService;
    }

    /**
     * tickOrchestrator exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ServerTickOrchestrator tickOrchestrator() {
        return tickOrchestrator;
    }

    /**
     * workerRuntimeService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public WorkerRuntimeService workerRuntimeService() {
        return tickOrchestrator.workerRuntimeService();
    }

    /**
     * handleAbortPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public WorkerRuntimeService.AbortResult handleAbortPacket(AbortWorkersPacket packet) {
        LogUtils.logDebug("Handling abort packet playerId={}", packet.playerId());
        AgentManager.get().clearAgents(packet.playerId());
        return workerRuntimeService().abortAllForPlayerWithStats(packet.playerId());
    }

    /**
     * handleComponentTogglePacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean handleComponentTogglePacket(ComponentTogglePacket packet) {
        LogUtils.logDebug("Handling component toggle feature={} enabled={}", packet.feature(), packet.enabled());
        ComponentLifecycle component = components.get(packet.feature());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component == null) {
            LogUtils.logWarn("Received toggle for unknown feature={}", packet.feature());
            return false;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (packet.enabled()) {
            component.enable();
        } else {
            component.disable();
        }
        return component.isEnabled();
    }

    /**
     * handlePlayerStateSyncPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SyncCoreService.PlayerSyncState handlePlayerStateSyncPacket(PlayerStateSyncPacket packet) {
        MAServerRootConfig authoritativeServerConfig = MAConfig_Base.getServerRootConfig();
        PlayerStateService.PlayerState current = playerStateService.getPlayerState(packet.playerId());
        SyncCoreService.PlayerSyncState previousSyncState = syncCoreService.getPlayerState(packet.playerId());
        boolean unchangedState = current.excavationToggled() == packet.excavationToggled()
            && current.shaftVentToggled() == packet.shaftVentToggled()
            && current.selectedExcavationShapeIndex() == packet.selectedExcavationShapeIndex()
            && current.selectedShaftanationShapeIndex() == packet.selectedShaftanationShapeIndex();
        boolean unchangedClientConfig = previousSyncState.clientConfig().equals(packet.clientConfig());
        boolean unchangedServerConfig = previousSyncState.serverConfig().equals(authoritativeServerConfig);
        if (unchangedState && unchangedClientConfig && unchangedServerConfig) {
            return previousSyncState;
        }

        LogUtils.logDebug("Synchronizing player state playerId={} commonConfig={}", packet.playerId(), authoritativeServerConfig.common());
        playerStateService.updatePlayerState(
            packet.playerId(),
            new PlayerStateService.PlayerState(
                packet.playerId(),
                current.hungerGuardActive(),
                current.lastHarvestTick(),
                current.recentHarvests(),
                packet.excavationToggled(),
                packet.shaftVentToggled(),
                packet.selectedExcavationShapeIndex(),
                packet.selectedShaftanationShapeIndex()
            )
        );
        SyncCoreService.PlayerSyncState state = syncCoreService.synchronize(
            packet.playerId(),
            packet.clientConfig(),
            authoritativeServerConfig,
            policyCoreService
        );
        MAConfig_Base.setClientRootConfig(state.clientConfig());
        MAConfig_Base.setServerRootConfig(state.serverConfig());
        tickOrchestrator.setTpsGuardActive(state.serverConfig().common().tpsGuard());
        tickOrchestrator.setProcessingDelay(state.serverConfig().common().enableTickDelay(), state.serverConfig().common().tickDelay());
        return state;
    }

    /**
     * handleFeatureDispatchPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void handleFeatureDispatchPacket(FeatureDispatchPacket packet) {
        LogUtils.logDebug(
            "Handling feature dispatch feature={} tool={} block={} pos=({}, {}, {})",
            packet.feature(),
            packet.toolId(),
            packet.blockId(),
            packet.blockX(),
            packet.blockY(),
            packet.blockZ()
        );
        FeatureEventHandler.onToolUse(
            packet.feature(),
            packet.blockX(),
            packet.blockY(),
            packet.blockZ(),
            packet.blockId(),
            packet.toolId()
        );
    }

    /**
     * handleIlluminationActionPacket routes explicit client illumination requests into the live server agent path.
     */
    public boolean handleIlluminationActionPacket(ServerPlayer player, IlluminationActionPacket packet) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player == null || packet == null) {
            return false;
        }

        ComponentLifecycle component = components.get(FeatureId.ILLUMINATION);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (component == null || !component.isEnabled()) {
            return false;
        }

        SyncedClientConfig effectiveConfig = syncCoreService.getPlayerState(player.getUUID().getLeastSignificantBits()).effectiveConfig();
        CommonConfig commonConfig = effectiveConfig == null ? defaultConfig.common() : effectiveConfig.common();
        IlluminationConfig illuminationConfig = effectiveConfig == null ? defaultConfig.illumination() : effectiveConfig.illumination();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (illuminationConfig == null || !illuminationConfig.enabled()) {
            return false;
        }

        BlockPos targetPos = new BlockPos(packet.blockX(), packet.blockY(), packet.blockZ());
        LogUtils.logDebug(
            "Handling illumination action player={} area={} target={}",
            player.getScoreboardName(),
            packet.area(),
            targetPos
        );

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!packet.area()) {
            AgentManager.get().addAgent(player, new IlluminationPlaceAgent(player, targetPos, packet.faceDirection(), illuminationConfig, commonConfig));
        } else {
            // Area mode intentionally centers on the player so targeting misses do not offset coverage.
            BlockPos origin = player.blockPosition().above();
            AgentManager.get().addAgent(player, new IlluminationAgent(player, origin, illuminationConfig, commonConfig));
        }
        return true;
    }

    /**
     * handleSupremeVantagePacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SupremeVantageService.RewardGrant handleSupremeVantagePacket(SupremeVantagePacket packet) {
        return supremeVantageService.grantNextReward(packet.playerId(), packet.code());
    }

    /**
     * handleSupremeVantagePacketGrantSpec exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public SupremeVantageService.ItemGrantSpec handleSupremeVantagePacketGrantSpec(SupremeVantagePacket packet) {
        SupremeVantageService.RewardGrant grant = handleSupremeVantagePacket(packet);
        return supremeVantageService.materializeRewardSpec(grant);
    }

    /**
     * commonConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CommonConfig commonConfig() {
        return defaultConfig.common();
    }

    /**
     * policyCoreService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public PolicyCoreService policyCoreService() {
        return policyCoreService;
    }

    /**
     * syncCoreService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SyncCoreService syncCoreService() {
        return syncCoreService;
    }

    /**
     * defaultConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SyncedClientConfig defaultConfig() {
        return defaultConfig;
    }

    /**
     * supremeVantageService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SupremeVantageService supremeVantageService() {
        return supremeVantageService;
    }

}
