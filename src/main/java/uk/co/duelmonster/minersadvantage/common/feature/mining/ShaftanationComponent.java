package uk.co.duelmonster.minersadvantage.common.feature.mining;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.ShaftanationCoreService;

public final class ShaftanationComponent implements ComponentLifecycle {
    private final ShaftanationConfig config;
    private final ShaftanationCoreService service;
    private boolean enabled;
    private int progressDepth;
    private ShaftanationCoreService.ShaftBatch lastBatch = new ShaftanationCoreService.ShaftBatch(0, 0, List.of());

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

    public int progressDepth() {
        return progressDepth;
    }

    public ShaftanationCoreService.ShaftBatch lastBatch() {
        return lastBatch;
    }

    @Override
    public void register() {
        enabled = false;
        progressDepth = 0;
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
        if (service.isStone(context.blockId())) {
            lastBatch = service.buildBatch(progressDepth, config.maxDepth(), config.processesPerTick());
            progressDepth = lastBatch.newDepth();
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        progressDepth = 0;
        lastBatch = new ShaftanationCoreService.ShaftBatch(0, 0, List.of());
    }
}
