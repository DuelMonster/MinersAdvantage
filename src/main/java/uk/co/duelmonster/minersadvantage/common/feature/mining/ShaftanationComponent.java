package uk.co.duelmonster.minersadvantage.common.feature.mining;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.ShaftanationCoreService;

public final class ShaftanationComponent implements ComponentLifecycle {
    private final ShaftanationConfig config;
    private final ShaftanationCoreService service;
    private boolean enabled;

    public ShaftanationComponent(ShaftanationConfig config) {
        this.config = config;
        this.service = new ShaftanationCoreService();
    }

    public ShaftanationCoreService service() {
        return service;
    }

    public ShaftanationConfig config() {
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
        // Processing via pickaxe event handlers
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
