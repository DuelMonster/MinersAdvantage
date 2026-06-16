package uk.co.duelmonster.minersadvantage.common.shape.api;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation.ExcavationFaceGeometry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftFloorGeometry;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft.ShaftShapeProcessor;

/**
 * Caches deterministic shape positions as origin-relative offsets so runtime calls can reuse precomputed geometry.
 */
public final class MAShapePrecomputeCache {
  private static final BlockPos PRECOMPUTE_ORIGIN = BlockPos.ZERO;
  private static final Set<String> NON_CACHEABLE_IDS = Set.of(MAShapeIds.EXCAVATION_SHAPELESS);
  private static final Map<CacheKey, Set<BlockPos>> PRECOMPUTED_OFFSETS = new ConcurrentHashMap<>();
  private static final Map<EnvelopeKey, Set<BlockPos>> PRECOMPUTED_EXCAVATION_ENVELOPES = new ConcurrentHashMap<>();

  private record CacheKey(
      String shapeId,
      int width,
      int height,
      int depth,
      Direction hitFace,
      Direction playerFacing,
      int floorOffset) {
  }

  private record EnvelopeKey(
      int width,
      int height,
      int depth,
      Direction hitFace,
      Direction playerFacing) {
  }

  private MAShapePrecomputeCache() {
  }

  /**
   * Warm all deterministic shape keys for current synced dimensions.
   */
  public static void warmupFromConfig(SyncedClientConfig config) {
    SyncedClientConfig value = config == null ? SyncedClientConfig.defaults() : config;
    MAShapeBootstrap.ensureInitialized();

    int before = PRECOMPUTED_OFFSETS.size();
    int envelopeBefore = PRECOMPUTED_EXCAVATION_ENVELOPES.size();
    warmupFeature(FeatureId.EXCAVATION, value.excavation().width(), value.excavation().height(),
        value.excavation().depth());
    warmupFeature(FeatureId.SHAFTANATION, value.shaftanation().width(), value.shaftanation().height(),
        value.shaftanation().depth());
    int after = PRECOMPUTED_OFFSETS.size();
    int envelopeAfter = PRECOMPUTED_EXCAVATION_ENVELOPES.size();
    int added = Math.max(0, after - before);
    int envelopesAdded = Math.max(0, envelopeAfter - envelopeBefore);

    if (added == 0 && envelopesAdded == 0) {
      return;
    }

    LogUtils.logDebug(
        "Shape precompute warmup complete entriesBefore={} entriesAfter={} added={} envelopesBefore={} envelopesAfter={} envelopesAdded={} excavationDims={}x{}x{} shaftDims={}x{}x{}",
        before,
        after,
        added,
        envelopeBefore,
        envelopeAfter,
        envelopesAdded,
        value.excavation().width(),
        value.excavation().height(),
        value.excavation().depth(),
        value.shaftanation().width(),
        value.shaftanation().height(),
        value.shaftanation().depth());
  }

  /**
   * Compute deterministic shape positions using precomputed offsets when available.
   */
  public static Set<BlockPos> compute(MAShapeDefinition shape, MAShapeContext context) {
    if (!isCacheable(shape.id())) {
      return shape.compute(context);
    }

    CacheKey key = keyFor(shape.id(), context);
    Set<BlockPos> offsets = PRECOMPUTED_OFFSETS.computeIfAbsent(key, unused -> precomputeOffsets(shape, key));

    if (offsets.isEmpty()) {
      return Set.of();
    }

    BlockPos origin = context.origin();
    HashSet<BlockPos> absolute = new HashSet<>(offsets.size());
    for (BlockPos relative : offsets) {
      absolute.add(origin.offset(relative.getX(), relative.getY(), relative.getZ()).immutable());
    }
    return absolute;
  }

  /**
   * Resolve precomputed excavation envelope offsets (includes shapeless envelope pre-calculation).
   */
  public static Set<BlockPos> excavationEnvelopeOffsets(
      int width,
      int height,
      int depth,
      Direction hitFace,
      Direction playerFacing) {
    EnvelopeKey key = new EnvelopeKey(
        Math.max(1, width),
        Math.max(1, height),
        Math.max(1, depth),
        hitFace == null ? Direction.NORTH : hitFace,
        playerFacing == null ? Direction.NORTH : playerFacing);
    return PRECOMPUTED_EXCAVATION_ENVELOPES.computeIfAbsent(key,
        MAShapePrecomputeCache::precomputeExcavationEnvelopeOffsets);
  }

  /**
   * Resolve absolute excavation envelope blocks at origin from cached relative offsets.
   */
  public static Set<BlockPos> excavationEnvelopeAt(
      BlockPos origin,
      int width,
      int height,
      int depth,
      Direction hitFace,
      Direction playerFacing) {
    Set<BlockPos> offsets = excavationEnvelopeOffsets(width, height, depth, hitFace, playerFacing);
    HashSet<BlockPos> absolute = new HashSet<>(offsets.size());
    for (BlockPos relative : offsets) {
      absolute.add(origin.offset(relative.getX(), relative.getY(), relative.getZ()).immutable());
    }
    return absolute;
  }

  private static void warmupFeature(FeatureId feature, int width, int height, int depth) {
    if (feature == FeatureId.EXCAVATION) {
      for (Direction hitFace : Direction.values()) {
        for (Direction playerFacing : EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
          excavationEnvelopeOffsets(width, height, depth, hitFace, playerFacing);
        }
      }
    }

    for (MAShapeDefinition shape : MAShapeRegistry.forFeature(feature)) {
      if (!isCacheable(shape.id())) {
        continue;
      }

      for (Direction hitFace : Direction.values()) {
        for (Direction playerFacing : EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
          for (int floorOffset : floorOffsetsFor(shape.id(), height)) {
            CacheKey key = new CacheKey(
                shape.id(),
                Math.max(1, width),
                Math.max(1, height),
                Math.max(1, depth),
                hitFace,
                playerFacing,
                floorOffset);
            PRECOMPUTED_OFFSETS.computeIfAbsent(key, unused -> precomputeOffsets(shape, key));
          }
        }
      }
    }
  }

  private static Set<BlockPos> precomputeExcavationEnvelopeOffsets(EnvelopeKey key) {
    ExcavationFaceGeometry.FaceDirection face = ExcavationFaceGeometry.fromMinecraftDirection(key.hitFace());
    ExcavationFaceGeometry.FaceDirection facing = ExcavationFaceGeometry.fromMinecraftDirection(key.playerFacing());
    ExcavationFaceGeometry.IntRange widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(key.width());
    ExcavationFaceGeometry.IntRange heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(key.height());

    HashSet<BlockPos> envelope = new HashSet<>();
    for (int d = 0; d < key.depth(); d++) {
      for (int y = heightRange.min(); y <= heightRange.max(); y++) {
        for (int w = widthRange.min(); w <= widthRange.max(); w++) {
          int[] offset = ExcavationFaceGeometry.offsetFor(face, facing, d, w, y);
          envelope.add(new BlockPos(offset[0], offset[1], offset[2]).immutable());
        }
      }
    }
    return Collections.unmodifiableSet(envelope);
  }

  private static Set<BlockPos> precomputeOffsets(MAShapeDefinition shape, CacheKey key) {
    if (MAShapeIds.SHAFTANATION_SHAFT.equals(shape.id())) {
      return Collections.unmodifiableSet(
          ShaftShapeProcessor.computeAtOrigin(
              key.hitFace(),
              key.playerFacing(),
              key.width(),
              key.height(),
              key.depth(),
              key.floorOffset()));
    }

    MAShapeContext context = new MAShapeContext(
        null,
        null,
        PRECOMPUTE_ORIGIN,
        null,
        key.hitFace(),
        key.playerFacing(),
        key.width(),
        key.height(),
        key.depth());
    Set<BlockPos> computed = shape.compute(context);
    return Collections.unmodifiableSet(new HashSet<>(computed));
  }

  private static CacheKey keyFor(String shapeId, MAShapeContext context) {
    int floorOffset = 0;
    if (MAShapeIds.SHAFTANATION_SHAFT.equals(shapeId) && context.player() != null) {
      floorOffset = ShaftFloorGeometry.resolveFloorOffset(
          context.origin().getY(),
          context.player().blockPosition().getY(),
          context.height());
    }
    return new CacheKey(
        shapeId,
        context.width(),
        context.height(),
        context.depth(),
        context.hitFace(),
        context.playerFacing(),
        floorOffset);
  }

  private static boolean isCacheable(String shapeId) {
    return !NON_CACHEABLE_IDS.contains(shapeId);
  }

  private static Set<Integer> floorOffsetsFor(String shapeId, int height) {
    if (!MAShapeIds.SHAFTANATION_SHAFT.equals(shapeId)) {
      return Set.of(0);
    }

    int normalizedHeight = Math.max(1, height);
    HashSet<Integer> offsets = new HashSet<>();
    offsets.add(0);
    for (int offset = -(normalizedHeight - 1); offset <= 0; offset++) {
      offsets.add(offset);
    }
    return offsets;
  }
}
