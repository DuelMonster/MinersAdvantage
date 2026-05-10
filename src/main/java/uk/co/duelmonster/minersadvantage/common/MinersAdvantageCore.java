package uk.co.duelmonster.minersadvantage.common;

import java.util.EnumMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.component.ComponentDescriptor;
import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentRegistry;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
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
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.core.ServerTickOrchestrator;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;

public final class MinersAdvantageCore {
    private final ComponentRegistry componentRegistry = new ComponentRegistry();
    private final Map<FeatureId, ComponentLifecycle> components = new EnumMap<>(FeatureId.class);
    private final PlayerStateService playerStateService;
    private final ServerTickOrchestrator tickOrchestrator;

    public MinersAdvantageCore() {
        this.playerStateService = new PlayerStateService();
        this.tickOrchestrator = new ServerTickOrchestrator(playerStateService);
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
        CaptivationConfig config = new CaptivationConfig(true, false, 16, 8, false, false);
        CaptivationComponent component = new CaptivationComponent(config);
        components.put(FeatureId.CAPTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("captivation", "Captivation", component));
    }

    private void registerCropination() {
        CropinationConfig config = new CropinationConfig(true, false);
        CropinationComponent component = new CropinationComponent(config);
        components.put(FeatureId.CROPINATION, component);
        componentRegistry.register(new ComponentDescriptor("cropination", "Cropination", component));
    }

    private void registerCultivation() {
        CultivationConfig config = new CultivationConfig(true, 4);
        CultivationComponent component = new CultivationComponent(config);
        components.put(FeatureId.CULTIVATION, component);
        componentRegistry.register(new ComponentDescriptor("cultivation", "Cultivation", component));
    }

    private void registerExcavation() {
        ExcavationConfig config = new ExcavationConfig(true, 3, 2, 10);
        ExcavationComponent component = new ExcavationComponent(config);
        components.put(FeatureId.EXCAVATION, component);
        componentRegistry.register(new ComponentDescriptor("excavation", "Excavation", component));
    }

    private void registerIllumination() {
        IlluminationConfig config = new IlluminationConfig(true, 2, 1);
        IlluminationComponent component = new IlluminationComponent(config);
        components.put(FeatureId.ILLUMINATION, component);
        componentRegistry.register(new ComponentDescriptor("illumination", "Illumination", component));
    }

    private void registerLumbination() {
        LumbinationConfig config = new LumbinationConfig(true, 3, 3, 8);
        LumbinationComponent component = new LumbinationComponent(config);
        components.put(FeatureId.LUMBINATION, component);
        componentRegistry.register(new ComponentDescriptor("lumbination", "Lumbination", component));
    }

    private void registerPathanation() {
        PathanationConfig config = new PathanationConfig(true, 6);
        PathanationComponent component = new PathanationComponent(config);
        components.put(FeatureId.PATHANATION, component);
        componentRegistry.register(new ComponentDescriptor("pathanation", "Pathanation", component));
    }

    private void registerShaftanation() {
        ShaftanationConfig config = new ShaftanationConfig(true, 30, 10);
        ShaftanationComponent component = new ShaftanationComponent(config);
        components.put(FeatureId.SHAFTANATION, component);
        componentRegistry.register(new ComponentDescriptor("shaftanation", "Shaftanation", component));
    }

    private void registerSubstitution() {
        SubstitutionConfig config = new SubstitutionConfig(true, false, true);
        SubstitutionComponent component = new SubstitutionComponent(config);
        components.put(FeatureId.SUBSTITUTION, component);
        componentRegistry.register(new ComponentDescriptor("substitution", "Substitution", component));
    }

    private void registerVeination() {
        VeinationConfig config = new VeinationConfig(true, 4);
        VeinationComponent component = new VeinationComponent(config);
        components.put(FeatureId.VEINATION, component);
        componentRegistry.register(new ComponentDescriptor("veination", "Veination", component));
    }

    private void registerVentilation() {
        VentilationConfig config = new VentilationConfig(true, 3, 2, 8);
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
}
