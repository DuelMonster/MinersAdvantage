package uk.co.duelmonster.minersadvantage.common.feature.utility;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.substitution.SubstitutionCoreService.ToolCandidate;

public final class SubstitutionComponent implements ComponentLifecycle {
    private final SubstitutionConfig config;
    private final SubstitutionCoreService service;
    private boolean enabled;
    private String lastSelectedToolId;
    private SubstitutionCoreService.SubstitutionDecision lastDecision =
        new SubstitutionCoreService.SubstitutionDecision(null, false, false, "idle");

    public SubstitutionComponent(SubstitutionConfig config) {
        this.config = config;
        this.service = new SubstitutionCoreService();
    }

    public SubstitutionCoreService service() {
        return service;
    }

    public SubstitutionConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public String lastSelectedToolId() {
        return lastSelectedToolId;
    }

    public SubstitutionCoreService.SubstitutionDecision lastDecision() {
        return lastDecision;
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
        boolean oreContext = context.blockId().contains("ore");
        boolean combatContext = context.blockId().startsWith("entity:") || context.toolId().contains("sword") || context.toolId().contains("combat");
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
            switchBackToPrimary
        );
        lastSelectedToolId = lastDecision.selectedToolId();
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastSelectedToolId = null;
        lastDecision = new SubstitutionCoreService.SubstitutionDecision(null, false, false, "idle");
    }
}
