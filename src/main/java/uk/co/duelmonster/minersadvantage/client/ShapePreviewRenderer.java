package uk.co.duelmonster.minersadvantage.client;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import uk.co.duelmonster.minersadvantage.client.KeyBindings.ClientAction;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.input.ClientInputService.ClientInputState;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeBootstrap;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDimensions;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

/**
 * ShapePreviewRenderer renders lightweight held-key shape previews on the client.
 */
public final class ShapePreviewRenderer {
    private static final int MAX_PREVIEW_PARTICLES = 72;

    private ShapePreviewRenderer() {
    }

    public static void renderHeldPreview(ClientInputState state, Set<ClientAction> pressedActions) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        boolean excavationPreview =
            pressedActions.contains(ClientAction.EXCAVATION_MODE_TOGGLE)
                && state.featureEnabled().getOrDefault(FeatureId.EXCAVATION, false);
        boolean shaftPreview =
            pressedActions.contains(ClientAction.SHAFT_VENT_TOGGLE)
                && state.featureEnabled().getOrDefault(FeatureId.SHAFTANATION, false);

        if (!excavationPreview && !shaftPreview) {
            return;
        }

        MAShapeBootstrap.ensureInitialized();
        Player player = minecraft.player;
        BlockPos origin = blockHit.getBlockPos();
        Direction hitFace = blockHit.getDirection();

        if (excavationPreview) {
            var excavation = MAServerRootConfig.defaults().excavation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.excavationFromRadii(excavation.radiusHorizontal(), excavation.radiusVertical());
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                hitFace,
                player.getDirection(),
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                MAX_PREVIEW_PARTICLES
            );
            MAShapeRegistry.byIndex(FeatureId.EXCAVATION, state.selectedExcavationShapeIndex())
                .ifPresent(shape -> spawnPreviewParticles(shape.compute(context)));
        }

        if (shaftPreview) {
            var shaft = MAServerRootConfig.defaults().shaftanation();
            MAShapeDimensions.Dimensions dimensions = MAShapeDimensions.shaftFromConfig(shaft.shaftWidth(), shaft.shaftHeight(), shaft.maxDepth());
            MAShapeContext context = new MAShapeContext(
                minecraft.level,
                player,
                origin,
                hitFace,
                player.getDirection(),
                dimensions.width(),
                dimensions.height(),
                dimensions.depth(),
                MAX_PREVIEW_PARTICLES
            );
            MAShapeRegistry.byIndex(FeatureId.SHAFTANATION, state.selectedShaftanationShapeIndex())
                .ifPresent(shape -> spawnPreviewParticles(shape.compute(context)));
        }
    }

    private static void spawnPreviewParticles(Set<BlockPos> positions) {
        if (positions.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int stride = Math.max(1, (int) Math.ceil((double) positions.size() / (double) MAX_PREVIEW_PARTICLES));
        int index = 0;
        for (BlockPos pos : positions) {
            if (index % stride == 0) {
                minecraft.level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5d,
                    pos.getY() + 0.5d,
                    pos.getZ() + 0.5d,
                    0.0d,
                    0.0d,
                    0.0d
                );
            }
            index++;
        }
    }
}
