package uk.co.duelmonster.minersadvantage.common.feature.utility;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationCoreService;

public final class VeinationComponent implements ComponentLifecycle {
    private final VeinationConfig config;
    private final VeinationCoreService service;
    private boolean enabled;
    private List<VeinationCoreService.VeinNode> lastVein = List.of();

    public VeinationComponent(VeinationConfig config) {
        this.config = config;
        this.service = new VeinationCoreService();
    }

    public VeinationCoreService service() {
        return service;
    }

    public VeinationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public List<VeinationCoreService.VeinNode> lastVein() {
        return lastVein;
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
        if (context.blockId().contains("ore") && service.sameVein(context.blockId(), context.blockId())) {
            int targetBlocks = service.estimatedBlocksInVein(config.maxVeinDistance(), 1);
            lastVein = service.buildVeinNodes(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                config.maxVeinDistance(),
                targetBlocks
            );
        } else {
            lastVein = List.of();
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastVein = List.of();
    }
}
