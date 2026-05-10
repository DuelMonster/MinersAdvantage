package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public final class CommonEventHandlerImpl implements ToolEventHandler {
    private final MinersAdvantageCore core;

    public CommonEventHandlerImpl(MinersAdvantageCore core) {
        this.core = core;
    }

    @Override
    public void onPickaxeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (blockId.contains("ore") || blockId.contains("stone")) {
            if (blockId.contains("deep") || blockId.contains("stone")) {
                FeatureEventHandler.onToolUse(FeatureId.SHAFTANATION, blockX, blockY, blockZ, blockId, "pickaxe");
            } else {
                FeatureEventHandler.onToolUse(FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "pickaxe");
            }
        }
    }

    @Override
    public void onShovelUse(int blockX, int blockY, int blockZ, String blockId) {
        if (blockId.contains("dirt") || blockId.contains("grass") || blockId.contains("sand")) {
            FeatureEventHandler.onToolUse(FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "shovel");
        }
    }

    @Override
    public void onHoeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (blockId.contains("crop")) {
            FeatureEventHandler.onToolUse(FeatureId.CROPINATION, blockX, blockY, blockZ, blockId, "hoe");
        } else if (blockId.contains("grass") || blockId.contains("dirt")) {
            FeatureEventHandler.onToolUse(FeatureId.CULTIVATION, blockX, blockY, blockZ, blockId, "hoe");
        }
    }

    @Override
    public void onAxeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (blockId.contains("log") || blockId.contains("stem")) {
            FeatureEventHandler.onToolUse(FeatureId.LUMBINATION, blockX, blockY, blockZ, blockId, "axe");
        }
    }

    @Override
    public void onItemPickup(String itemId, boolean isDirectPickup) {
        // Captivation handles item pickup events
        FeatureEventHandler.onToolUse(FeatureId.CAPTIVATION, 0, 0, 0, "item:" + itemId, "hand");
    }

    @Override
    public void onServerTick() {
        core.serverTick();
    }
}
