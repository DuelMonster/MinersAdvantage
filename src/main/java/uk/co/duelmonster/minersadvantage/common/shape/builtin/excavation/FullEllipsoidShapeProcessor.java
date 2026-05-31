package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class FullEllipsoidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry.fromMinecraftDirection(context.hitFace());

        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);
        double rz = Math.max(0.5d, context.depth() / 2.0d);
        double centerDepth = context.depth() / 2.0d;

        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());
        int minH = ShapeGeometryUtils.minCenteredOffset(context.height());
        int maxH = ShapeGeometryUtils.maxCenteredOffset(context.height());

        for (int d = 0; d <= context.depth(); d++) {
            double dz = (d - centerDepth) / rz;
            for (int y = minH; y <= maxH; y++) {
                double ny = y / ry;
                for (int w = minW; w <= maxW; w++) {
                    if (out.size() >= context.maxBlocks()) {
                        return out;
                    }
                    double nx = w / rx;
                    if ((nx * nx) + (ny * ny) + (dz * dz) <= 1.0d) {
                        int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, d, w, y);
                        BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
                        out.add(pos);
                    }
                }
            }
        }

        return out;
    }
}
