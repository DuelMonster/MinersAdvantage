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
        List<ToolCandidate> candidates = List.of(
            new ToolCandidate("mainhand", oreContext ? 8.0 : 7.0, 0, 1, false),
            new ToolCandidate("silk_pick", oreContext ? 8.2 : 6.5, 1, 0, false),
            new ToolCandidate("damaged", 9.0, 0, 2, !config.allowMending())
        );
        ToolCandidate best = service.selectBest(candidates, config.prioritizeSilkTouch(), oreContext);
        lastSelectedToolId = best == null ? null : best.id();
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastSelectedToolId = null;
    }
}
