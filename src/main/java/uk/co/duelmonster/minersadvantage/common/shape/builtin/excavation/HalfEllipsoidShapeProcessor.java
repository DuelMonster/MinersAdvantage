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
        ExcavationFaceGeometry.ComputeState state = ExcavationFaceGeometry.begin(context);
        var widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.width());
        var heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.height());

        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);

        int totalDepth = Math.max(1, context.depth());
        var crossSections = ExcavationFaceGeometry.depthCrossSectionTracker(
            "half_ellipsoid",
            context,
            totalDepth,
            0
        );

        for (int d = 0; d < totalDepth; d++) {
            // True half-ellipsoid depth profile: 0 at the hit face, 1 at the far tail.
            double dz = totalDepth <= 1 ? 0.0d : (double) d / (totalDepth - 1);

            for (int y = heightRange.min(); y <= heightRange.max(); y++) {
                double ny = y / ry;
                for (int w = widthRange.min(); w <= widthRange.max(); w++) {
                    double nx = w / rx;
                    if ((nx * nx) + (ny * ny) + (dz * dz) <= 1.0d) {
                        ExcavationFaceGeometry.addOffset(state, d, w, y);
                        crossSections.record(d, w, y);
                    }
                }
            }
        }
        crossSections.log();
        return state.out();
    }
}
