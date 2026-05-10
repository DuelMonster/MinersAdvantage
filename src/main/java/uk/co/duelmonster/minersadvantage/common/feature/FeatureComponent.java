package uk.co.duelmonster.minersadvantage.common.feature;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;

public final class FeatureComponent implements ComponentLifecycle {
    private final FeatureId id;
    private boolean enabled;

    public FeatureComponent(FeatureId id) {
        this.id = id;
    }

    public FeatureId id() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void register() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void tick() {
        // Each feature will inject event or tick behavior in loader adapters.
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
