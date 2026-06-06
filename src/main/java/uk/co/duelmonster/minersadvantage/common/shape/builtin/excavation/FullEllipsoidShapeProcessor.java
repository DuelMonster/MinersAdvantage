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
        ExcavationFaceGeometry.ComputeState state = ExcavationFaceGeometry.begin(context);
        var widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.width());
        var heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.height());

        // Clamp radii so tiny configs still produce a meaningful shape instead of divide-by-zero chaos.
        double rx = Math.max(0.5d, context.width() / 2.0d);
        double ry = Math.max(0.5d, context.height() / 2.0d);

        int totalDepth = Math.max(1, context.depth());
        var crossSections = ExcavationFaceGeometry.depthCrossSectionTracker(
            "full_ellipsoid",
            context,
            totalDepth,
            0
        );

        for (int d = 0; d < totalDepth; d++) {
            // True full-ellipsoid depth profile: 1 at both ends and 0 at the center.
            double dz;
            if (totalDepth <= 1) {
                dz = 0.0d;
            } else {
                double t = (2.0d * d) / (totalDepth - 1);
                dz = Math.abs(t - 1.0d);
            }

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
