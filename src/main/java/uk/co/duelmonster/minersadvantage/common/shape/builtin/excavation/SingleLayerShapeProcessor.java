package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Single-layer excavation processor that carves one plane oriented by hit face.
 */
public final class SingleLayerShapeProcessor implements MAShapeProcessor {
    /**
     * Compute a one-layer shape using different traversal logic for vertical vs horizontal face hits.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        Direction hitFace = context.hitFace();
        ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry.fromMinecraftDirection(context.hitFace());

        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());

        // Vertical faces map to a flat XZ plane around origin.
        if (hitFace.getAxis().isVertical()) {
            int minD = ShapeGeometryUtils.minCenteredOffset(context.depth());
            int maxD = ShapeGeometryUtils.maxCenteredOffset(context.depth());

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int depthOffset = minD; depthOffset <= maxD; depthOffset++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = minW; w <= maxW; w++) {
                    BlockPos pos = origin.offset(w, 0, depthOffset).immutable();
                    out.add(pos);
                }
            }

            return out;
        }

        // Horizontal faces push forward by depth while keeping Y fixed to preserve single-layer behavior.
        for (int d = 0; d <= context.depth(); d++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int w = minW; w <= maxW; w++) {
                int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, d, w, 0);
                BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
                out.add(pos);
            }
        }

        return out;
    }
}
