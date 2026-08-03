package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * IlluminationCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class IlluminationCoreService {
  private int torchStackCount = 0;
  private int torchIndex = -1;

  /**
   * playerHasTorches exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean playerHasTorches(ServerPlayer player) {
    getTorchSlot(player);
    return torchIndex >= 0;
  }

  /**
   * getTorchSlot exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public void getTorchSlot(ServerPlayer player) {
    torchStackCount = 0;
    torchIndex = -1;

    Item torchItem = Blocks.TORCH.asItem();
    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
      ItemStack stack = player.getInventory().getItem(slot);
      if (stack != null && stack.getItem().equals(torchItem)) {
        torchStackCount++;
        torchIndex = Functions.getSlotFromInventory(player, stack);
      }
    }
  }

  /**
   * getTorchablePositionsInArea exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public List<BlockPos> getTorchablePositionsInArea(Level world, AABB area) {
    List<BlockPos> positions = new ArrayList<>();
    BlockPos previousPos = null;

    for (double y = area.minY; y <= area.maxY; y++) {
      for (double x = area.minX; x <= area.maxX; x++) {
        for (double z = area.minZ; z <= area.maxZ; z++) {
          BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
          if (isTorchablePosition(world, pos)
              && (previousPos == null || !Functions.isWithinRange(previousPos, pos, 5))) {
            positions.add(pos);
            previousPos = pos;
          }
        }
      }
    }
    return positions;
  }

  /**
   * isTorchablePosition exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean isTorchablePosition(Level world, BlockPos pos) {
    return world.isEmptyBlock(pos) && (!world.isEmptyBlock(pos.below())
        || !world.isEmptyBlock(pos.north())
        || !world.isEmptyBlock(pos.east())
        || !world.isEmptyBlock(pos.south())
        || !world.isEmptyBlock(pos.west()));
  }

  /**
   * canPlaceTorchOnFace exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean canPlaceTorchOnFace(Level world, BlockPos pos, Direction face) {
    BlockState state = world.getBlockState(pos);
    Block block = state.getBlock();

    boolean validFace = face != Direction.DOWN
        && state.isFaceSturdy(world, pos, face)
        && world.getBlockState(pos.relative(face)).canBeReplaced();
    boolean validBlockType = block != Blocks.END_GATEWAY && block != Blocks.JACK_O_LANTERN;

    return validFace && validBlockType;
  }

  /**
   * IlluminationDecision keeps this part of MinersAdvantage running without turning server ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
   */
  public record IlluminationDecision(
      TorchPlacement placement,
      int plannedTorches,
      boolean placeNow,
      boolean manualMode,
      boolean inventoryDepleted) {
  }

  /**
   * shouldPlaceTorch exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean shouldPlaceTorch(int lightLevel) {
    return lightLevel < 8;
  }

  /**
   * selectPlacement exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public TorchPlacement selectPlacement(boolean leftWallAvailable, boolean rightWallAvailable) {
    if (leftWallAvailable && rightWallAvailable) {
      return TorchPlacement.BOTH_WALLS;
    } else if (leftWallAvailable) {
      return TorchPlacement.LEFT_WALL;
    } else if (rightWallAvailable) {
      return TorchPlacement.RIGHT_WALL;
    }
    return TorchPlacement.FLOOR;
  }

  /**
   * expectedPlacementsInRadius exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public int expectedPlacementsInRadius(int radiusHorizontal, int radiusVertical) {
    return (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
  }

  /**
   * isManualMode exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean isManualMode(String toolHint) {
    return RegistryPredicates.isManualToolMode(toolHint);
  }

  /**
   * selectPlacement exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public TorchPlacement selectPlacement(boolean leftWallAvailable, boolean rightWallAvailable, String toolHint) {
    if (RegistryPredicates.isManualLeftMode(toolHint)) {
      return leftWallAvailable ? TorchPlacement.LEFT_WALL : TorchPlacement.FLOOR;
    }
    if (RegistryPredicates.isManualRightMode(toolHint)) {
      return rightWallAvailable ? TorchPlacement.RIGHT_WALL : TorchPlacement.FLOOR;
    }
    if (toolHint.contains("manual_both") || toolHint.contains("manual_wall")) {
      if (leftWallAvailable && rightWallAvailable) {
        return TorchPlacement.BOTH_WALLS;
      }
      if (leftWallAvailable) {
        return TorchPlacement.LEFT_WALL;
      }
      if (rightWallAvailable) {
        return TorchPlacement.RIGHT_WALL;
      }
    }
    if (toolHint.contains("manual_floor")) {
      return TorchPlacement.FLOOR;
    }
    return selectPlacement(leftWallAvailable, rightWallAvailable);
  }

  /**
   * availableTorches exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public int availableTorches(String toolHint, int requestedTorches) {
    if (toolHint.contains("empty")) {
      return 0;
    }
    if (toolHint.contains("single")) {
      return 1;
    }
    return requestedTorches;
  }

  public IlluminationDecision decidePlacement(
      int lightLevel,
      boolean leftWallAvailable,
      boolean rightWallAvailable,
      int radiusHorizontal,
      int radiusVertical,
      String toolHint) {
    boolean manualMode = isManualMode(toolHint);
    if (!shouldPlaceTorch(lightLevel)) {
      return new IlluminationDecision(TorchPlacement.FLOOR, 0, false, manualMode, false);
    }

    TorchPlacement placement = selectPlacement(leftWallAvailable, rightWallAvailable, toolHint);
    int placementMultiplier = placement == TorchPlacement.BOTH_WALLS ? 2 : 1;
    int requested = Math.max(1,
        (expectedPlacementsInRadius(radiusHorizontal, radiusVertical) / 3) * placementMultiplier);
    int available = availableTorches(toolHint, requested);
    boolean inventoryDepleted = available < requested;
    int planned = Math.min(requested, available);
    boolean placeNow = planned > 0;
    return new IlluminationDecision(placement, planned, placeNow, manualMode, inventoryDepleted);
  }
}
