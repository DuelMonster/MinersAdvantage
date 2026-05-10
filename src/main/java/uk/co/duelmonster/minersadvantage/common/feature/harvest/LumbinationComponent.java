package uk.co.duelmonster.minersadvantage.common.feature.harvest;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.harvest.LumbinationCoreService;

public final class LumbinationComponent implements ComponentLifecycle {
    private final LumbinationConfig config;
    private final LumbinationCoreService service;
    private boolean enabled;

    public LumbinationComponent(LumbinationConfig config) {
        this.config = config;
        this.service = new LumbinationCoreService();
    }

    public LumbinationCoreService service() {
        return service;
    }

    public LumbinationConfig config() {
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
        if (!ComponentTickHelper.shouldExecute(isEnabled())) {
            return;
        }

        var context = ComponentTickHelper.getContext();
        if (service.isLog(context.blockId())) {
            // Fell connected tree logs up to maxTrunkRange
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
