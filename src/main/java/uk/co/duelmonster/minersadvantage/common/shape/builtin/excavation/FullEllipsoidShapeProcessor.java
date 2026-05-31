package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

/**
 * Full ellipsoid excavation that keeps everything inside the normalized 3D radius equation.
 */
public final class FullEllipsoidShapeProcessor implements MAShapeProcessor {
    /**
     * Compute a full ellipsoid volume centered along depth so both front and back halves are represented.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        var plan = ExcavationFaceGeometry.beginWithCenteredWidthAndHeight(context);

        // Clamp radii so tiny configs still produce a meaningful shape instead of divide-by-zero chaos.
        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);
        double rz = Math.max(0.5d, context.depth() / 2.0d);
        double centerDepth = context.depth() / 2.0d;

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int d = 0; d <= context.depth(); d++) {
            double dz = (d - centerDepth) / rz;
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int y = plan.heightRange().min(); y <= plan.heightRange().max(); y++) {
                double ny = y / ry;
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = plan.widthRange().min(); w <= plan.widthRange().max(); w++) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!ExcavationFaceGeometry.hasCapacity(plan.state(), context)) {
                        return plan.state().out();
                    }
                    double nx = w / rx;
                    // Classic ellipsoid test; if it's inside the unit sphere, it earns a block slot.
                    if ((nx * nx) + (ny * ny) + (dz * dz) <= 1.0d) {
                        ExcavationFaceGeometry.addOffset(plan.state(), d, w, y);
                    }
                }
            }
        }

        return plan.state().out();
    }
}
