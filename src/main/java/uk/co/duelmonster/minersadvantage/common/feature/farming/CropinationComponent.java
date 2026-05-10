package uk.co.duelmonster.minersadvantage.common.feature.farming;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.services.farming.CropinationCoreService;

import uk.co.duelmonster.minersadvantage.common.services.farming.CropinationCoreService.CropAction;

public final class CropinationComponent implements ComponentLifecycle {
    private final CropinationConfig config;
    private final CropinationCoreService service;
    private boolean enabled;
    private CropAction lastAction = new CropAction(false, false, 0, 0);

    public CropinationComponent(CropinationConfig config) {
        this.config = config;
        this.service = new CropinationCoreService();
    }

    public CropinationCoreService service() {
        return service;
    }

    public CropinationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public CropAction lastAction() {
        return lastAction;
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
        if (context.blockId().contains("crop")) {
            int cropAge = context.blockId().contains("mature") ? 7 : 6;
            int availableSeeds = config.harvestSeeds() ? 8 : 0;
            lastAction = service.evaluateCrop(cropAge, 7, availableSeeds, config.harvestSeeds(), 5, 5);
        } else {
            lastAction = new CropAction(false, false, 0, 0);
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastAction = new CropAction(false, false, 0, 0);
    }
}
