package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

public final class ThreeByThreeGeometry {
    public enum FaceAxis {
        X,
        Y,
        Z
    }

    private ThreeByThreeGeometry() {
    }

    public static int[][] offsetsForFaceAxis(FaceAxis faceAxis) {
        int[][] offsets = new int[9][3];
        int index = 0;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                int dx = 0;
                int dy = 0;
                int dz = 0;
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
