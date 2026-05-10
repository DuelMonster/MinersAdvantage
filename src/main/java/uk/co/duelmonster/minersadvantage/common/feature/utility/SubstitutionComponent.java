package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.SubstitutionCoreService;
import uk.co.duelmonster.minersadvantage.common.services.utility.ToolCandidate;

public final class SubstitutionComponent implements ComponentLifecycle {
    private final SubstitutionConfig config;
    private final SubstitutionCoreService service;
    private boolean enabled;

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

        // Select optimal tool from inventory based on block and preferences
        ToolCandidate primary = new ToolCandidate(100, 0, 0, false);
        ToolCandidate secondary = new ToolCandidate(50, 1, 0, false);
        var best = service.selectBest(primary, secondary, config.prioritizeSilkTouch());
        // Equip best tool
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
