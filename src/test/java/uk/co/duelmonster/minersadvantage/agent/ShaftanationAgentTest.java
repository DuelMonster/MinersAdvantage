package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Geometry regression tests for Shaftanation torch placement behavior.
 * If these fail, torches are probably decorating the wrong wall and players will absolutely notice.
 */
class ShaftanationAgentTest {
    /**
     * Ensure left/right wall torch modes stay aligned with shaft travel direction.
     */
    @Test
    /**
     * w al lt or ch ge om et ry fo ll ow ss ha ft fa ci ng fo rl ef ta nd ri gh tm od es exists so this path stays predictable and easier to debug when things get weird.
     */
    void wallTorchGeometryFollowsShaftFacingForLeftAndRightModes() {
        assertWallTorchGeometry(0, -1, true, -1, 1, 0);
        assertWallTorchGeometry(0, -1, false, 1, 1, 0);
        assertWallTorchGeometry(0, 1, true, 1, 1, 0);
        assertWallTorchGeometry(0, 1, false, -1, 1, 0);
        assertWallTorchGeometry(1, 0, true, 0, 1, -1);
        assertWallTorchGeometry(1, 0, false, 0, 1, 1);
        assertWallTorchGeometry(-1, 0, true, 0, 1, 1);
        assertWallTorchGeometry(-1, 0, false, 0, 1, -1);
    }

    /**
     * Ensure wall mode samples light at floor level, not at wall torch height.
     */
    @Test
    /**
     * w al lt or ch ge om et ry sa mp le sl ig ht fr om th es ha ft fl oo r exists so this path stays predictable and easier to debug when things get weird.
     */
    void wallTorchGeometrySamplesLightFromTheShaftFloor() {
        ShaftanationAgent.TorchGeometry floorGeometry = ShaftanationAgent.floorTorchGeometry();
        ShaftanationAgent.TorchGeometry wallGeometry = ShaftanationAgent.wallTorchGeometry(0, 1, 1, true);

        assertEquals(0, floorGeometry.offsetY());
        assertEquals(0, floorGeometry.lightCheckOffsetY());
        assertEquals(1, wallGeometry.offsetY());
        assertEquals(0, wallGeometry.lightCheckOffsetY());
    }

    /**
     * Shared assertion helper so each direction case stays readable instead of becoming a giant copy/paste wall.
     */
    private static void assertWallTorchGeometry(
        int shaftStepX,
        int shaftStepZ,
        boolean leftWall,
        int expectedOffsetX,
        int expectedOffsetY,
        int expectedOffsetZ
    ) {
        ShaftanationAgent.TorchGeometry geometry = ShaftanationAgent.wallTorchGeometry(shaftStepX, shaftStepZ, 1, leftWall);
        assertEquals(expectedOffsetX, geometry.offsetX());
        assertEquals(expectedOffsetY, geometry.offsetY());
        assertEquals(expectedOffsetZ, geometry.offsetZ());
        assertEquals(0, geometry.lightCheckOffsetY());
    }
}
