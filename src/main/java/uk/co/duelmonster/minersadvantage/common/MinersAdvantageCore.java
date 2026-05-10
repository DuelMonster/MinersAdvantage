package uk.co.duelmonster.minersadvantage.common;

import java.util.EnumMap;
import java.util.Map;
import uk.co.duelmonster.minersadvantage.common.component.ComponentDescriptor;
import uk.co.duelmonster.minersadvantage.common.component.ComponentRegistry;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureComponent;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public final class MinersAdvantageCore {
    private final ComponentRegistry componentRegistry = new ComponentRegistry();
    private final Map<FeatureId, FeatureComponent> components = new EnumMap<>(FeatureId.class);

    public void bootstrap() {
        for (FeatureId featureId : FeatureId.values()) {
            FeatureComponent component = new FeatureComponent(featureId);
            components.put(featureId, component);
            componentRegistry.register(new ComponentDescriptor(featureId.name().toLowerCase(), featureId.name(), component));
        }
        componentRegistry.enableAll();
    }

    public void shutdown() {
        componentRegistry.disableAll();
        componentRegistry.cleanupAll();
    }

    public Map<FeatureId, FeatureComponent> components() {
        return Map.copyOf(components);
    }

    public void serverTick() {
        componentRegistry.tickAll();
    }
}
