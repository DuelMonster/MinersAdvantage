package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShaftanationAgentTest {
    @Test
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

    @Test
    void wallTorchGeometrySamplesLightFromTheShaftFloor() {
        ShaftanationAgent.TorchGeometry floorGeometry = ShaftanationAgent.floorTorchGeometry();
        ShaftanationAgent.TorchGeometry wallGeometry = ShaftanationAgent.wallTorchGeometry(0, 1, 1, true);

        assertEquals(0, floorGeometry.offsetY());
        assertEquals(0, floorGeometry.lightCheckOffsetY());
        assertEquals(1, wallGeometry.offsetY());
        assertEquals(0, wallGeometry.lightCheckOffsetY());
    }

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