package uk.co.duelmonster.minersadvantage.common.feature.mining;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.ExcavationCoreService;

public final class ExcavationComponent implements ComponentLifecycle {
    private final ExcavationConfig config;
    private final ExcavationCoreService service;
    private boolean enabled;
    private List<ExcavationCoreService.ExcavationTarget> lastPlan = List.of();

    public ExcavationComponent(ExcavationConfig config) {
        this.config = config;
        this.service = new ExcavationCoreService();
    }

    public ExcavationCoreService service() {
        return service;
    }

    public ExcavationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public List<ExcavationCoreService.ExcavationTarget> lastPlan() {
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
        if (service.isBlock(context.blockId())) {
            int verticalRadius = context.toolId().contains("shovel") ? 0 : config.radiusVertical();
            lastPlan = service.buildPlan(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                context.blockId(),
                config.radiusHorizontal(),
                verticalRadius,
                config.processesPerTick()
            );
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastPlan = List.of();
    }
}
