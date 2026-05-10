package uk.co.duelmonster.minersadvantage.common.feature.farming;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.farming.CropinationCoreService;

public final class CropinationComponent implements ComponentLifecycle {
    private final CropinationConfig config;
    private final CropinationCoreService service;
    private boolean enabled;

    public CropinationComponent(CropinationConfig config) {
        this.config = config;
        this.service = new CropinationCoreService();
    }

    public CropinationCoreService service() {
        return service;
    }

    public CropinationConfig config() {
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
        // Processed via hoe event handlers in loader adapters
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
