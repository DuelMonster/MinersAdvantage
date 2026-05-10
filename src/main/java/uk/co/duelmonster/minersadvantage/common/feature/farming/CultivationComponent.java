package uk.co.duelmonster.minersadvantage.common.feature.farming;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.services.farming.FarmingCoreService;

public final class CultivationComponent implements ComponentLifecycle {
    private final CultivationConfig config;
    private final FarmingCoreService service;
    private boolean enabled;

    public CultivationComponent(CultivationConfig config) {
        this.config = config;
        this.service = new FarmingCoreService();
    }

    public FarmingCoreService service() {
        return service;
    }

    public CultivationConfig config() {
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
        // Till action processing via hoe event handlers
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
