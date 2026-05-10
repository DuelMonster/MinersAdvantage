package uk.co.duelmonster.minersadvantage.common.component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ComponentRegistry {
    private final Map<String, ComponentDescriptor> descriptors = new LinkedHashMap<>();

    public void register(ComponentDescriptor descriptor) {
        descriptors.put(descriptor.id(), descriptor);
        descriptor.component().register();
    }

    public Collection<ComponentDescriptor> all() {
        return descriptors.values();
    }

    public void enableAll() {
        descriptors.values().forEach(d -> d.component().enable());
    }

    public void disableAll() {
        descriptors.values().forEach(d -> d.component().disable());
    }

    public void tickAll() {
        descriptors.values().forEach(d -> d.component().tick());
    }

    public void cleanupAll() {
        descriptors.values().forEach(d -> d.component().cleanup());
    }
}
