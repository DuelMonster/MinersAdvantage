package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationCoreService;

public final class VeinationComponent implements ComponentLifecycle {
    private final VeinationConfig config;
    private final VeinationCoreService service;
    private boolean enabled;

    public VeinationComponent(VeinationConfig config) {
        this.config = config;
        this.service = new VeinationCoreService();
    }

    public VeinationCoreService service() {
        return service;
    }

    public VeinationConfig config() {
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
        // Processing during ore harvest event handlers
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
