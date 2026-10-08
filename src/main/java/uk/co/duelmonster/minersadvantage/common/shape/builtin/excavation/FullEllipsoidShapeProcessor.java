package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.LinkedHashSet;
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
        int totalDepth = Math.max(1, context.depth());
        var crossSections = ExcavationFaceGeometry.depthCrossSectionTracker(
            "full_ellipsoid",
            context,
            totalDepth,
            0
        );

        Set<BlockPos> positions = computePositions(
            context.origin(),
            ExcavationFaceGeometry.fromMinecraftDirection(context.hitFace()),
            ExcavationFaceGeometry.fromMinecraftDirection(context.playerFacing()),
            context.width(),
            context.height(),
            totalDepth,
            crossSections);
        crossSections.log();
        return positions;
    }

    private static Set<BlockPos> computePositions(
        BlockPos origin,
        ExcavationFaceGeometry.FaceDirection hitFace,
        ExcavationFaceGeometry.FaceDirection playerFacing,
        int width,
        int height,
        int depth,
        ExcavationFaceGeometry.DepthCrossSectionTracker crossSections) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int totalDepth = Math.max(1, depth);
        var widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(safeWidth);
        var heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(safeHeight);

        for (int d = 0; d < totalDepth; d++) {
            double dz = normalizedDepth(d, totalDepth);

            for (int y = heightRange.min(); y <= heightRange.max(); y++) {
                double ny = normalizedOffset(y, safeHeight);
                for (int w = widthRange.min(); w <= widthRange.max(); w++) {
                    double nx = normalizedOffset(w, safeWidth);
                    if (isInsideEllipsoid(nx, ny, dz)) {
                        ExcavationFaceGeometry.addOffset(out, origin, hitFace, playerFacing, d, w, y);
                        if (crossSections != null) {
                            crossSections.record(d, w, y);
                        }
                    }
                }
            }
        }

        return out;
    }

    static double normalizedOffset(int offset, int dimension) {
        double radius = Math.max(0.5d, Math.max(1, dimension) / 2.0d);
        return offset / radius;
    }

    static double normalizedDepth(int depthIndex, int totalDepth) {
        int safeDepth = Math.max(1, totalDepth);
        if (safeDepth <= 1) {
            return 0.0d;
        }

        double position = (2.0d * depthIndex) / (safeDepth - 1);
        return Math.abs(position - 1.0d);
    }

    static boolean isInsideEllipsoid(double normalizedWidth, double normalizedHeight, double normalizedDepth) {
        return (normalizedWidth * normalizedWidth)
            + (normalizedHeight * normalizedHeight)
            + (normalizedDepth * normalizedDepth) <= 1.0d;
    }
}
