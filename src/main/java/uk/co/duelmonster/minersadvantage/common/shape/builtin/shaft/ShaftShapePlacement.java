package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;

import java.util.LinkedHashSet;

public final class ShaftShapePlacement {
    private ShaftShapePlacement() {
    }

    public static boolean addIfCapacity(
        LinkedHashSet<BlockPos> out,
        MAShapeContext context,
        BlockPos base,
        Direction right,
        int lateralOffset,
        int verticalOffset
    ) {
        if (out.size() >= context.maxBlocks()) {
            return false;
        }
        out.add(base.relative(right, lateralOffset).offset(0, verticalOffset, 0).immutable());
        return true;
    }
}