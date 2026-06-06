package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fixed 3x3 excavation processor used for the classic plane-style mining footprint.
 */
public final class ThreeByThreeShapeProcessor implements MAShapeProcessor {
    /**
     * Compute the 3x3 plane around origin based on hit-face axis.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        ThreeByThreeGeometry.FaceAxis faceAxis = toFaceAxis(context.hitFace());

        // Offset table already encodes the shape, so this loop is basically "stamp and go".
        for (int[] offset : ThreeByThreeGeometry.offsetsForFaceAxis(faceAxis)) {
            BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
            out.add(pos);
        }

        return out;
    }

    /**
     * Convert Minecraft direction axis into ThreeByThreeGeometry axis enum.
     */
    private static ThreeByThreeGeometry.FaceAxis toFaceAxis(Direction hitFace) {
        return switch (hitFace.getAxis()) {
            case X -> ThreeByThreeGeometry.FaceAxis.X;
            case Y -> ThreeByThreeGeometry.FaceAxis.Y;
            case Z -> ThreeByThreeGeometry.FaceAxis.Z;
        };
    }
}
