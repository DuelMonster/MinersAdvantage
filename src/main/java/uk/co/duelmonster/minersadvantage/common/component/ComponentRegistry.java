package uk.co.duelmonster.minersadvantage.common.component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ComponentRegistry keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ComponentRegistry {
    private final Map<String, ComponentDescriptor> descriptors = new LinkedHashMap<>();

    /**
     * register exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void register(ComponentDescriptor descriptor) {
        descriptors.put(descriptor.id(), descriptor);
        descriptor.component().register();
    }

    /**
     * all exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public Collection<ComponentDescriptor> all() {
        return descriptors.values();
    }

    /**
     * enableAll exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void enableAll() {
        descriptors.values().forEach(d -> d.component().enable());
    }

    /**
     * disableAll exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void disableAll() {
        descriptors.values().forEach(d -> d.component().disable());
    }

    /**
     * tickAll exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void tickAll() {
        descriptors.values().forEach(d -> d.component().tick());
    }

    /**
     * cleanupAll exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanupAll() {
        descriptors.values().forEach(d -> d.component().cleanup());
    }
}
