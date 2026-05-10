package uk.co.duelmonster.minersadvantage.common.feature.mining;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.ExcavationCoreService;

public final class ExcavationComponent implements ComponentLifecycle {
    private final ExcavationConfig config;
    private final ExcavationCoreService service;
    private boolean enabled;

    public ExcavationComponent(ExcavationConfig config) {
        this.config = config;
        this.service = new ExcavationCoreService();
    }

    public ExcavationCoreService service() {
        return service;
    }

    public ExcavationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
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
        // Processing via shovel event handlers
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
