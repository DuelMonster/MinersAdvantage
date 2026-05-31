package uk.co.duelmonster.minersadvantage.common.feature.utility;

import java.util.List;
import java.util.Set;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService.ToolCandidate;

/**
 * SubstitutionComponent keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class SubstitutionComponent implements ComponentLifecycle {
    private final SubstitutionConfig config;
    private final SubstitutionCoreService service;
    private boolean enabled;
    private String lastSelectedToolId;
    private SubstitutionCoreService.SubstitutionDecision lastDecision =
        new SubstitutionCoreService.SubstitutionDecision(null, false, false, "idle");

    /**
     * SubstitutionComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionComponent(SubstitutionConfig config) {
        this.config = config;
        this.service = new SubstitutionCoreService();
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionConfig config() {
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
     * lastSelectedToolId exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public String lastSelectedToolId() {
        return lastSelectedToolId;
    }

    /**
     * lastDecision exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public SubstitutionCoreService.SubstitutionDecision lastDecision() {
        return lastDecision;
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!ComponentTickHelper.shouldExecute(isEnabled())) {
            return;
        }

        var context = ComponentTickHelper.getContext();
        boolean oreContext = RegistryPredicates.isOreLikeBlockId(context.blockId());
        boolean combatContext = RegistryPredicates.isContextEntity(context.blockId())
            || RegistryPredicates.isSwordToolId(context.toolId())
            || RegistryPredicates.isCombatToolId(context.toolId());
        boolean switchBackToPrimary = !oreContext
            && !combatContext
            && lastSelectedToolId != null
            && !lastSelectedToolId.equals(context.toolId());
        List<ToolCandidate> candidates = List.of(
            new ToolCandidate(context.toolId(), oreContext ? 8.0 : 7.0, 0, 0, combatContext ? 6 : 2, false, false),
            new ToolCandidate("silk_pick", oreContext ? 8.2 : 6.5, 1, 0, 2, false, false),
            new ToolCandidate("fortune_pick", oreContext ? 7.9 : 6.8, 0, 3, 2, false, false),
            new ToolCandidate("battle_blade", 5.0, 0, 0, 9, false, false),
            new ToolCandidate("mending_pick", 8.4, 0, 2, 1, false, true)
        );
        lastDecision = service.decideTool(
            context.toolId(),
            candidates,
            config.allowMending(),
            oreContext && config.prioritizeSilkTouch(),
            oreContext && !config.prioritizeSilkTouch(),
            combatContext,
            switchBackToPrimary,
            Set.copyOf(config.blacklist())
        );
        lastSelectedToolId = lastDecision.selectedToolId();
    }

    @Override
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastSelectedToolId = null;
        lastDecision = new SubstitutionCoreService.SubstitutionDecision(null, false, false, "idle");
    }
}
