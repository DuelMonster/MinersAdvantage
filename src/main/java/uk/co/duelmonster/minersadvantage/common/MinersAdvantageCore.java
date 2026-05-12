package uk.co.duelmonster.minersadvantage.common;

import java.util.EnumMap;
import java.util.Map;
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
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
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
import uk.co.duelmonster.minersadvantage.common.network.AbortWorkersPacket;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.network.SupremeVantagePacket;
import uk.co.duelmonster.minersadvantage.common.event.FeatureEventHandler;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.core.ServerTickOrchestrator;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;
import uk.co.duelmonster.minersadvantage.common.services.sync.SyncCoreService;
import uk.co.duelmonster.minersadvantage.common.services.utility.SupremeVantageService;

/**
 * MinersAdvantageCore keeps this part of Miners Advantage running without turning server ticks into confetti.
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
    private final ServerOverridesConfig defaultServerOverrides;

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
        this.defaultConfig = SyncedClientConfig.defaults();
        this.defaultServerOverrides = new ServerOverridesConfig();
    }

    /**
     * bootstrap exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void bootstrap() {
        registerCaptivation();
        registerCropination();
        registerCultivation();
        registerExcavation();
        registerIllumination();
        registerLumbination();
        registerPathanation();
        registerShaftanation();
        registerSubstitution();
        registerVeination();
        registerVentilation();
        componentRegistry.enableAll();
    }

    /**
     * registerCaptivation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerCaptivation() {
        CaptivationConfig config = defaultConfig.captivation();
        CaptivationComponent component = new CaptivationComponent(config);
        components.put(FeatureId.CAPTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("captivation", "Captivation", component));
    }

    /**
     * registerCropination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerCropination() {
        CropinationConfig config = defaultConfig.cropination();
        CropinationComponent component = new CropinationComponent(config);
        components.put(FeatureId.CROPINATION, component);
        componentRegistry.register(new ComponentDescriptor("cropination", "Cropination", component));
    }

    /**
     * registerCultivation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerCultivation() {
        CultivationConfig config = defaultConfig.cultivation();
        CultivationComponent component = new CultivationComponent(config);
        components.put(FeatureId.CULTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("cultivation", "Cultivation", component));
    }

    /**
     * registerExcavation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerExcavation() {
        ExcavationConfig config = defaultConfig.excavation();
        ExcavationComponent component = new ExcavationComponent(config);
        components.put(FeatureId.EXCAVATION, component);
        componentRegistry.register(new ComponentDescriptor("excavation", "Excavation", component));
    }

    /**
     * registerIllumination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerIllumination() {
        IlluminationConfig config = defaultConfig.illumination();
        IlluminationComponent component = new IlluminationComponent(config);
        components.put(FeatureId.ILLUMINATION, component);
        componentRegistry.register(new ComponentDescriptor("illumination", "Illumination", component));
    }

    /**
     * registerLumbination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerLumbination() {
        LumbinationConfig config = defaultConfig.lumbination();
        LumbinationComponent component = new LumbinationComponent(config);
        components.put(FeatureId.LUMBINATION, component);
        componentRegistry.register(new ComponentDescriptor("lumbination", "Lumbination", component));
    }

    /**
     * registerPathanation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerPathanation() {
        PathanationConfig config = defaultConfig.pathanation();
        PathanationComponent component = new PathanationComponent(config);
        components.put(FeatureId.PATHANATION, component);
        componentRegistry.register(new ComponentDescriptor("pathanation", "Pathanation", component));
    }

    /**
     * registerShaftanation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerShaftanation() {
        ShaftanationConfig config = defaultConfig.shaftanation();
        ShaftanationComponent component = new ShaftanationComponent(config);
        components.put(FeatureId.SHAFTANATION, component);
        componentRegistry.register(new ComponentDescriptor("shaftanation", "Shaftanation", component));
    }

    /**
     * registerSubstitution exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerSubstitution() {
        SubstitutionConfig config = defaultConfig.substitution();
        SubstitutionComponent component = new SubstitutionComponent(config);
        components.put(FeatureId.SUBSTITUTION, component);
        componentRegistry.register(new ComponentDescriptor("substitution", "Substitution", component));
    }

    /**
     * registerVeination exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerVeination() {
        VeinationConfig config = defaultConfig.veination();
        VeinationComponent component = new VeinationComponent(config);
        components.put(FeatureId.VEINATION, component);
        componentRegistry.register(new ComponentDescriptor("veination", "Veination", component));
    }

    /**
     * registerVentilation exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void registerVentilation() {
        VentilationConfig config = defaultConfig.ventilation();
        VentilationComponent component = new VentilationComponent(config);
        components.put(FeatureId.VENTILATION, component);
        componentRegistry.register(new ComponentDescriptor("ventilation", "Ventilation", component));
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
        return workerRuntimeService().abortAllForPlayerWithStats(packet.playerId());
    }

    /**
     * handleComponentTogglePacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean handleComponentTogglePacket(ComponentTogglePacket packet) {
        ComponentLifecycle component = components.get(packet.feature());
        if (component == null) {
            return false;
        }

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
        return syncCoreService.synchronize(
            packet.playerId(),
            packet.clientConfig(),
            packet.serverConfig(),
            packet.serverOverrides(),
            policyCoreService
        );
    }

    /**
     * handleFeatureDispatchPacket exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void handleFeatureDispatchPacket(FeatureDispatchPacket packet) {
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

    /**
     * defaultServerOverrides exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ServerOverridesConfig defaultServerOverrides() {
        return defaultServerOverrides;
    }
}



