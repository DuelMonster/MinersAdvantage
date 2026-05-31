package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class WideCuboidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry.fromMinecraftDirection(context.hitFace());

        int minDepthSide = ShapeGeometryUtils.minCenteredOffset(context.depth());
        int maxDepthSide = ShapeGeometryUtils.maxCenteredOffset(context.depth());
        int minH = ShapeGeometryUtils.minCenteredOffset(context.height());
        int maxH = ShapeGeometryUtils.maxCenteredOffset(context.height());

        for (int forwardStep = 0; forwardStep <= context.width(); forwardStep++) {
            for (int y = minH; y <= maxH; y++) {
                for (int side = minDepthSide; side <= maxDepthSide; side++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, forwardStep, side, y);
                    BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
                    out.add(pos);
                }
            }
        }

        return out;
    }
}
