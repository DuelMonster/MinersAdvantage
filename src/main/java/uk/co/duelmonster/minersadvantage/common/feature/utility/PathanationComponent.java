package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.PathanationCoreService;

public final class PathanationComponent implements ComponentLifecycle {
    private final PathanationConfig config;
    private final PathanationCoreService service;
    private boolean enabled;

    public PathanationComponent(PathanationConfig config) {
        this.config = config;
        this.service = new PathanationCoreService();
    }

    public PathanationCoreService service() {
        return service;
    }

    public PathanationConfig config() {
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
