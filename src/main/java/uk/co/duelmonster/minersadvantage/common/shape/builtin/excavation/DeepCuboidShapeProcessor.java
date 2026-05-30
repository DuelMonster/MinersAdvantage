package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DeepCuboidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        Direction forward = ShapeGeometryUtils.forwardFromContext(context);
        Direction right = ShapeGeometryUtils.rightFromForward(forward);

        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());
        int minH = ShapeGeometryUtils.minCenteredOffset(context.height());
        int maxH = ShapeGeometryUtils.maxCenteredOffset(context.height());

        for (int d = 0; d <= context.depth(); d++) {
            for (int y = minH; y <= maxH; y++) {
                for (int w = minW; w <= maxW; w++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    BlockPos pos = origin.relative(forward, d).relative(right, w).offset(0, y, 0).immutable();
                    out.add(pos);
                }
            }
        }
        return out;
    }
}
