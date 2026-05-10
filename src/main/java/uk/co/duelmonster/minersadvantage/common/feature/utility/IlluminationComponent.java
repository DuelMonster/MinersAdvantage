package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.IlluminationCoreService;

public final class IlluminationComponent implements ComponentLifecycle {
    private final IlluminationConfig config;
    private final IlluminationCoreService service;
    private boolean enabled;

    public IlluminationComponent(IlluminationConfig config) {
        this.config = config;
        this.service = new IlluminationCoreService();
    }

    public IlluminationCoreService service() {
        return service;
    }

    public IlluminationConfig config() {
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
        // Processing during feature execution event handlers
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
