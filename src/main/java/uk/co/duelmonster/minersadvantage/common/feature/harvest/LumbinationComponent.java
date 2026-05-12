package uk.co.duelmonster.minersadvantage.common.feature.harvest;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.harvest.LumbinationCoreService;
import uk.co.duelmonster.minersadvantage.common.services.harvest.LumbinationCoreService.LumbinationPlan;

import java.util.List;

/**
 * LumbinationComponent keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class LumbinationComponent implements ComponentLifecycle {
    private final LumbinationConfig config;
    private final LumbinationCoreService service;
    private boolean enabled;
    private LumbinationPlan lastPlan = new LumbinationPlan(0, 0, false, List.of());

    /**
     * LumbinationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public LumbinationComponent(LumbinationConfig config) {
        this.config = config;
        this.service = new LumbinationCoreService();
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public LumbinationCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public LumbinationConfig config() {
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
    public LumbinationPlan lastPlan() {
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
     * Optimized tick method to improve runtime performance and reduce unnecessary computations.
     */
    public void tick() {
        if (!ComponentTickHelper.shouldExecute(isEnabled())) {
            return;
        }

        var context = ComponentTickHelper.getContext();
        if (context == null || context.blockId() == null) {
            return;
        }

        if (service.isLog(context.blockId())) {
            lastPlan = service.buildPlan(
                Math.max(1, config.maxTrunkRange()),
                config.maxTrunkRange(),
                config.maxLeafRange(),
                config.processesPerTick(),
                config.replantSaplings()
            );
        } else if (service.isLeaf(context.blockId())) {
            lastPlan = service.buildPlan(
                Math.max(1, config.maxLeafRange()),
                config.maxTrunkRange(),
                config.maxLeafRange(),
                config.processesPerTick(),
                false
            );
        } else if (service.isSapling(context.blockId())) {
            lastPlan = service.buildPlan(0, config.maxTrunkRange(), config.maxLeafRange(), 1, true);
        }
    }

    @Override
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastPlan = new LumbinationPlan(0, 0, false, List.of());
    }
}



