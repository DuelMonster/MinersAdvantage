package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

public final class FullEllipsoidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        var plan = ExcavationFaceGeometry.beginWithCenteredWidthAndHeight(context);

        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);
        double rz = Math.max(0.5d, context.depth() / 2.0d);
        double centerDepth = context.depth() / 2.0d;

        for (int d = 0; d <= context.depth(); d++) {
            double dz = (d - centerDepth) / rz;
            for (int y = plan.heightRange().min(); y <= plan.heightRange().max(); y++) {
                double ny = y / ry;
                for (int w = plan.widthRange().min(); w <= plan.widthRange().max(); w++) {
                    if (!ExcavationFaceGeometry.hasCapacity(plan.state(), context)) {
                        return plan.state().out();
                    }
                    double nx = w / rx;
                    if ((nx * nx) + (ny * ny) + (dz * dz) <= 1.0d) {
                        ExcavationFaceGeometry.addOffset(plan.state(), d, w, y);
                    }
                }
            }
        }

        return plan.state().out();
    }
}
