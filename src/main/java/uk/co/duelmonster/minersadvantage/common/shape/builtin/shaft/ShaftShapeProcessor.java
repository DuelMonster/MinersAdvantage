package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ShaftShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        if (context.hitFace().getAxis().isVertical()) {
            return Set.of();
        }

        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        Direction forward = ShapeGeometryUtils.forwardFromContext(context);
        Direction right = ShapeGeometryUtils.rightFromForward(forward);
        BlockPos origin = context.origin();

        int playerFeetY = context.player().blockPosition().getY();
        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());

        for (int d = 0; d < context.depth(); d++) {
            BlockPos base = new BlockPos(origin.getX(), playerFeetY, origin.getZ()).relative(forward, d);
            for (int h = 0; h < context.height(); h++) {
                for (int w = minW; w <= maxW; w++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    out.add(base.relative(right, w).offset(0, h, 0).immutable());
                }
            }
        }
        return out;
    }
}
