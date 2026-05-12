package uk.co.duelmonster.minersadvantage.common.feature.farming;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.services.farming.FarmingCoreService;

/**
 * CultivationComponent keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CultivationComponent implements ComponentLifecycle {
    private final CultivationConfig config;
    private final FarmingCoreService service;
    private boolean enabled;
    private List<FarmingCoreService.CultivationStep> lastPlan = List.of();

    /**
     * CultivationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CultivationComponent(CultivationConfig config) {
        this.config = config;
        this.service = new FarmingCoreService();
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public FarmingCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CultivationConfig config() {
        return config;
    }

    /**
     * isEnabled exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    /**
     * lastPlan exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<FarmingCoreService.CultivationStep> lastPlan() {
        return lastPlan;
    }

    @Override
    /**
     * register exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void register() {
        enabled = false;
    }

    @Override
    /**
     * enable exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void enable() {
        enabled = true;
    }

    @Override
    /**
     * disable exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void disable() {
        enabled = false;
    }

    @Override
    /**
     * tick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastPlan = List.of();
    }
}
