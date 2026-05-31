package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SingleLayerShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        Direction hitFace = context.hitFace();
        ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry.fromMinecraftDirection(context.hitFace());

        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());

        if (hitFace.getAxis().isVertical()) {
            int minD = ShapeGeometryUtils.minCenteredOffset(context.depth());
            int maxD = ShapeGeometryUtils.maxCenteredOffset(context.depth());

            for (int depthOffset = minD; depthOffset <= maxD; depthOffset++) {
                for (int w = minW; w <= maxW; w++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    BlockPos pos = origin.offset(w, 0, depthOffset).immutable();
                    out.add(pos);
                }
            }

            return out;
        }

        for (int d = 0; d <= context.depth(); d++) {
            for (int w = minW; w <= maxW; w++) {
                if (out.size() >= context.maxBlocks()) {
                    return out;
                }
                int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, d, w, 0);
                BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
                out.add(pos);
            }
        }

        return out;
    }
}
