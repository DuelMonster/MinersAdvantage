package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

/**
 * Helper for deterministic 3x3 offset generation based on the axis of the clicked face.
 */
public final class ThreeByThreeGeometry {
    /**
     * Axis enum for selecting which plane receives the 3x3 stencil.
     */
    public enum FaceAxis {
        X,
        Y,
        Z
    }

    /**
     * Utility class only; all behavior is static.
     */
    private ThreeByThreeGeometry() {
    }

    /**
     * Produce the nine offset vectors that make up a 3x3 plane for the chosen axis.
     */
    public static int[][] offsetsForFaceAxis(FaceAxis faceAxis) {
        int[][] offsets = new int[9][3];
        int index = 0;
        // Two nested loops generate the classic 3x3 stencil from -1..1 on each plane axis.
        for (int a = -1; a <= 1; a++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int b = -1; b <= 1; b++) {
                int dx = 0;
                int dy = 0;
                int dz = 0;
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                switch (faceAxis) {
                    case X -> {
                        dy = a;
                        dz = b;
                    }
                    case Y -> {
                        dx = a;
                        dz = b;
                    }
                    case Z -> {
                        dx = a;
                        dy = b;
                    }
                }
                offsets[index][0] = dx;
                offsets[index][1] = dy;
                offsets[index][2] = dz;
                index++;
            }
        }
        return offsets;
    }
}
