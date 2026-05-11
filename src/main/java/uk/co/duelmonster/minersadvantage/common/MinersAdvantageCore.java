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

    public MinersAdvantageCore() {
        this.playerStateService = new PlayerStateService();
        this.tickOrchestrator = new ServerTickOrchestrator(playerStateService);
        this.policyCoreService = new PolicyCoreService();
        this.syncCoreService = new SyncCoreService();
        this.supremeVantageService = new SupremeVantageService();
        this.defaultConfig = SyncedClientConfig.defaults();
        this.defaultServerOverrides = new ServerOverridesConfig();
    }

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

    private void registerCaptivation() {
        CaptivationConfig config = defaultConfig.captivation();
        CaptivationComponent component = new CaptivationComponent(config);
        components.put(FeatureId.CAPTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("captivation", "Captivation", component));
    }

    private void registerCropination() {
        CropinationConfig config = defaultConfig.cropination();
        CropinationComponent component = new CropinationComponent(config);
        components.put(FeatureId.CROPINATION, component);
        componentRegistry.register(new ComponentDescriptor("cropination", "Cropination", component));
    }

    private void registerCultivation() {
        CultivationConfig config = defaultConfig.cultivation();
        CultivationComponent component = new CultivationComponent(config);
        components.put(FeatureId.CULTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("cultivation", "Cultivation", component));
    }

    private void registerExcavation() {
        ExcavationConfig config = defaultConfig.excavation();
        ExcavationComponent component = new ExcavationComponent(config);
        components.put(FeatureId.EXCAVATION, component);
        componentRegistry.register(new ComponentDescriptor("excavation", "Excavation", component));
    }

    private void registerIllumination() {
        IlluminationConfig config = defaultConfig.illumination();
        IlluminationComponent component = new IlluminationComponent(config);
        components.put(FeatureId.ILLUMINATION, component);
        componentRegistry.register(new ComponentDescriptor("illumination", "Illumination", component));
    }

    private void registerLumbination() {
        LumbinationConfig config = defaultConfig.lumbination();
        LumbinationComponent component = new LumbinationComponent(config);
        components.put(FeatureId.LUMBINATION, component);
        componentRegistry.register(new ComponentDescriptor("lumbination", "Lumbination", component));
    }

    private void registerPathanation() {
        PathanationConfig config = defaultConfig.pathanation();
        PathanationComponent component = new PathanationComponent(config);
        components.put(FeatureId.PATHANATION, component);
        componentRegistry.register(new ComponentDescriptor("pathanation", "Pathanation", component));
    }

    private void registerShaftanation() {
        ShaftanationConfig config = defaultConfig.shaftanation();
        ShaftanationComponent component = new ShaftanationComponent(config);
        components.put(FeatureId.SHAFTANATION, component);
        componentRegistry.register(new ComponentDescriptor("shaftanation", "Shaftanation", component));
    }

    private void registerSubstitution() {
        SubstitutionConfig config = defaultConfig.substitution();
        SubstitutionComponent component = new SubstitutionComponent(config);
        components.put(FeatureId.SUBSTITUTION, component);
        componentRegistry.register(new ComponentDescriptor("substitution", "Substitution", component));
    }

    private void registerVeination() {
        VeinationConfig config = defaultConfig.veination();
        VeinationComponent component = new VeinationComponent(config);
        components.put(FeatureId.VEINATION, component);
        componentRegistry.register(new ComponentDescriptor("veination", "Veination", component));
    }

    private void registerVentilation() {
        VentilationConfig config = defaultConfig.ventilation();
        VentilationComponent component = new VentilationComponent(config);
        components.put(FeatureId.VENTILATION, component);
        componentRegistry.register(new ComponentDescriptor("ventilation", "Ventilation", component));
    }

    public void shutdown() {
        componentRegistry.disableAll();
        componentRegistry.cleanupAll();
    }

    public Map<FeatureId, ComponentLifecycle> components() {
        return Map.copyOf(components);
    }

    public void serverTick() {
        tickOrchestrator.onServerTick();
        componentRegistry.tickAll();
    }

    public PlayerStateService playerStateService() {
        return playerStateService;
    }

    public ServerTickOrchestrator tickOrchestrator() {
        return tickOrchestrator;
    }

    public WorkerRuntimeService workerRuntimeService() {
        return tickOrchestrator.workerRuntimeService();
    }

    public WorkerRuntimeService.AbortResult handleAbortPacket(AbortWorkersPacket packet) {
        return workerRuntimeService().abortAllForPlayerWithStats(packet.playerId());
    }

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

    public SyncCoreService.PlayerSyncState handlePlayerStateSyncPacket(PlayerStateSyncPacket packet) {
        return syncCoreService.synchronize(
            packet.playerId(),
            packet.clientConfig(),
            packet.serverConfig(),
            packet.serverOverrides(),
            policyCoreService
        );
    }

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

    public SupremeVantageService.RewardGrant handleSupremeVantagePacket(SupremeVantagePacket packet) {
        return supremeVantageService.grantNextReward(packet.playerId(), packet.code());
    }

    public CommonConfig commonConfig() {
        return defaultConfig.common();
    }

    public PolicyCoreService policyCoreService() {
        return policyCoreService;
    }

    public SyncCoreService syncCoreService() {
        return syncCoreService;
    }

    public SyncedClientConfig defaultConfig() {
        return defaultConfig;
    }

    public SupremeVantageService supremeVantageService() {
        return supremeVantageService;
    }

    public ServerOverridesConfig defaultServerOverrides() {
        return defaultServerOverrides;
    }
}
