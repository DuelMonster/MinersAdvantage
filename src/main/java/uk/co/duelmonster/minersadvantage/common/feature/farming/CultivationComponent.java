package uk.co.duelmonster.minersadvantage.common.feature.farming;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.services.farming.FarmingCoreService;

public final class CultivationComponent implements ComponentLifecycle {
    private final CultivationConfig config;
    private final FarmingCoreService service;
    private boolean enabled;
    private List<FarmingCoreService.CultivationStep> lastPlan = List.of();

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

    public List<FarmingCoreService.CultivationStep> lastPlan() {
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
        if (context.blockId().contains("dirt") || context.blockId().contains("grass")) {
            lastPlan = service.buildCultivationPlan(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                config.hydrationDistance(),
                Math.max(1, config.hydrationDistance())
            );
        } else {
            lastPlan = List.of();
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastPlan = List.of();
    }
}
