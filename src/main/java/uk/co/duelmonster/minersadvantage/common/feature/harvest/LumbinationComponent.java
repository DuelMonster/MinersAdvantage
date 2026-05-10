package uk.co.duelmonster.minersadvantage.common.feature.harvest;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.harvest.LumbinationCoreService;
import uk.co.duelmonster.minersadvantage.common.services.harvest.LumbinationCoreService.LumbinationPlan;

import java.util.List;

public final class LumbinationComponent implements ComponentLifecycle {
    private final LumbinationConfig config;
    private final LumbinationCoreService service;
    private boolean enabled;
    private LumbinationPlan lastPlan = new LumbinationPlan(0, 0, false, List.of());

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

    public LumbinationPlan lastPlan() {
        return lastPlan;
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
            int connectedLogs = Math.max(1, Math.min(config.maxTrunkRange(), 6));
            boolean saplingAvailable = context.toolId().contains("axe");
            lastPlan = service.buildPlan(
                connectedLogs,
                config.maxTrunkRange(),
                config.maxLeafRange(),
                config.processesPerTick(),
                saplingAvailable
            );
        } else {
            lastPlan = new LumbinationPlan(0, 0, false, List.of());
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastPlan = new LumbinationPlan(0, 0, false, List.of());
    }
}
