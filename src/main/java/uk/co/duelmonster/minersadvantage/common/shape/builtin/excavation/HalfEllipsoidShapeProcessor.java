package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

/**
 * Half-ellipsoid excavation that starts at the origin plane and expands forward only.
 */
public final class HalfEllipsoidShapeProcessor implements MAShapeProcessor {
    /**
     * Compute the forward half of an ellipsoid using normalized distance checks per candidate point.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        var plan = ExcavationFaceGeometry.beginWithCenteredWidthAndHeight(context);

        // Keep widths/heights sane and use full depth as forward radius for one-sided ellipsoid shaping.
        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);
        double rz = Math.max(1.0d, context.depth());

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int d = 0; d <= context.depth(); d++) {
            double dz = d / rz;
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
                    // Same ellipsoid equation as full mode, but dz only grows from zero outward.
                    if ((nx * nx) + (ny * ny) + (dz * dz) <= 1.0d) {
                        ExcavationFaceGeometry.addOffset(plan.state(), d, w, y);
                    }
                }
            }
        }
        return plan.state().out();
    }
}
