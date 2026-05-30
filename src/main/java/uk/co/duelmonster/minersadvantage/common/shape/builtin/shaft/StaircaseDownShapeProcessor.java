package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class StaircaseDownShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        Direction forward = context.hitFace().getAxis().isVertical()
            ? ShapeGeometryUtils.horizontalOrNorth(context.playerFacing())
            : ShapeGeometryUtils.forwardFromContext(context);
        Direction right = ShapeGeometryUtils.rightFromForward(forward);
        BlockPos origin = context.origin();

        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());

        for (int d = 0; d < context.depth(); d++) {
            BlockPos depthBase = origin.relative(forward, d).offset(0, -d, 0);
            for (int h = 0; h < context.height(); h++) {
                for (int w = minW; w <= maxW; w++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    out.add(depthBase.relative(right, w).offset(0, -h, 0).immutable());
                }
            }
        }

        return out;
    }
}
