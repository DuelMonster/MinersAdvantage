package uk.co.duelmonster.minersadvantage.common.shape.api;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.DeepCuboidShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.FullEllipsoidShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.HalfEllipsoidShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.SingleLayerShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.ThreeByThreeShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.WideCuboidShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.StaircaseDownShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.StaircaseUpShapeProcessor;

/**
 * MAShapeBootstrap registers built-in shape definitions exactly once.
 */
public final class MAShapeBootstrap {
    private static boolean initialized = false;

    private MAShapeBootstrap() {
    }

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_DEEP_CUBOID, "Deep Cuboid", FeatureId.EXCAVATION, new DeepCuboidShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_WIDE_CUBOID, "Wide Cuboid", FeatureId.EXCAVATION, new WideCuboidShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_HALF_ELLIPSOID, "Half Ellipsoid", FeatureId.EXCAVATION, new HalfEllipsoidShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_FULL_ELLIPSOID, "Full Ellipsoid", FeatureId.EXCAVATION, new FullEllipsoidShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_SINGLE_LAYER, "Single Layer", FeatureId.EXCAVATION, new SingleLayerShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.EXCAVATION_THREE_BY_THREE, "3x3", FeatureId.EXCAVATION, new ThreeByThreeShapeProcessor()));

        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.SHAFTANATION_SHAFT, "Shaft", FeatureId.SHAFTANATION, new ShaftShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.SHAFTANATION_STAIRCASE_UP, "Staircase Up", FeatureId.SHAFTANATION, new StaircaseUpShapeProcessor()));
        MAShapeRegistry.register(new MAShapeDefinition(MAShapeIds.SHAFTANATION_STAIRCASE_DOWN, "Staircase Down", FeatureId.SHAFTANATION, new StaircaseDownShapeProcessor()));

        initialized = true;
    }
}
