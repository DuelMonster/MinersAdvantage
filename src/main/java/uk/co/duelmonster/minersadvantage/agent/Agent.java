package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationRuntimeService;

import java.lang.reflect.Method;

/**
 * Base class for all runtime agents that do feature work over multiple ticks.
 * Think of it as the shared survival kit: lifecycle flags, torch placement helpers, and veination fan-out glue.
 */
public abstract class Agent {
  private static volatile boolean useItemOnLookupWarned = false;
  private int ticksUntilNextProcessingWindow = 0;

  protected record BreakOutcome(boolean broken, ItemStack toolAfterBreak) {
  }

  protected final ServerPlayer player;
  protected final Level world;
  protected boolean complete = false;

  /**
   * Bind this agent to player and world context once at construction time.
   */
  public Agent(ServerPlayer player) {
    this.player = player;
    this.world = player.level();
    if (!(this instanceof CaptivationAgent)) {
      LogUtils.logDebug("Created {} for player={} dimension={}", getClass().getSimpleName(), player.getScoreboardName(),
          player.level().dimension());
    }
  }

  /**
   * Called every server tick. Returns true if the agent is finished and should be removed.
   */
  public abstract boolean tick();

  /**
   * Quick status check used by managers and tests.
   */
  public boolean isComplete() {
    return complete;
  }

  /**
   * Mark agent as done and log why, because future debugging always starts with "why did it stop?".
   */
  protected boolean finish(String reason) {
    complete = true;
    if (!(this instanceof CaptivationAgent)) {
      LogUtils.logDebug("Completed {} for player={} reason={}", getClass().getSimpleName(), player.getScoreboardName(),
          reason);
    }
    return true;
  }

  /**
   * Applies per-agent cadence so heavy workers can run every N ticks instead of every tick.
   */
  protected boolean shouldProcessThisTick(int ticksPerBlock) {
    int effectiveTicksPerBlock = Math.max(1, ticksPerBlock);
    if (effectiveTicksPerBlock <= 1) {
      return true;
    }

    if (ticksUntilNextProcessingWindow > 0) {
      ticksUntilNextProcessingWindow--;
      return false;
    }

    ticksUntilNextProcessingWindow = effectiveTicksPerBlock - 1;
    return true;
  }

  /**
   * Places a torch at the given position using the correct block state for the facing direction,
   * consuming one torch from the player's inventory. Returns false if the player has no torches.
   * Pass {@code null} or {@code Direction.UP} for a floor torch; any horizontal direction for a wall torch.
   */
  protected boolean placeTorchWithInventory(BlockPos pos, Direction facing) {
    int slot = findTorchSlot();
    if (slot < 0) {
      return false;
    }
    // Bail early if placement rules fail so we don't consume inventory for impossible placements.
    if (!canPlaceTorchAt(pos, facing)) {
      return false;
    }
    Direction placementDirection = normalizeTorchFacing(facing);
    BlockPos supportPos = placementDirection == Direction.UP ? pos.below()
        : pos.relative(placementDirection.getOpposite());
    return placeItemFromInventoryByUse(slot, supportPos, placementDirection);
  }

  /**
   * Validate both target replaceability and support sturdiness before attempting torch placement.
   */
  protected boolean canPlaceTorchAt(BlockPos pos, Direction facing) {
    Direction placementDirection = normalizeTorchFacing(facing);
    if (!world.getBlockState(pos).canBeReplaced()) {
      return false;
    }

    BlockPos supportPos = placementDirection == Direction.UP ? pos.below()
        : pos.relative(placementDirection.getOpposite());
    BlockState supportState = world.getBlockState(supportPos);
    return supportState.isFaceSturdy(world, supportPos, placementDirection)
        && torchStateForFacing(placementDirection).canSurvive(world, pos);
  }

  /**
   * Shared helper for agents that must avoid carving into blocked headspace.
   */
  protected boolean isAirOrReplaceableAbove(BlockPos pos) {
    BlockState above = world.getBlockState(pos.above());
    return above.isAir() || above.canBeReplaced();
  }

  /**
   * Produce torch block state for floor or wall placement based on normalized facing.
   */
  private BlockState torchStateForFacing(Direction facing) {
    Direction placementDirection = normalizeTorchFacing(facing);
    return placementDirection == Direction.UP
        ? Blocks.TORCH.defaultBlockState()
        : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, placementDirection);
  }

  /**
   * Normalize null/down/up facing into floor torch mode; only horizontal facings create wall torches.
   */
  private Direction normalizeTorchFacing(Direction facing) {
    return facing == null || facing == Direction.UP || facing == Direction.DOWN ? Direction.UP : facing;
  }

  /**
   * Returns the first inventory slot containing torches, or -1 if none.
   */
  protected int findTorchSlot() {
    return findFirstInventorySlot(Items.TORCH);
  }

  /**
   * Search inventory for first slot containing the requested item.
   */
  protected int findFirstInventorySlot(Item item) {
    Inventory inventory = player.getInventory();
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      if (inventory.getItem(i).is(item)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Perform item placement via game-mode use call while temporarily moving stack into offhand.
   */
  protected boolean placeItemFromInventoryByUse(int slot, BlockPos supportPos, Direction clickedFace) {
    if (player.gameMode == null) {
      return false;
    }

    Inventory inventory = player.getInventory();
    if (slot < 0 || slot >= inventory.getContainerSize()) {
      return false;
    }

    ItemStack sourceStackRef = inventory.getItem(slot);
    boolean sourceSlotIsOffhand = sourceStackRef == player.getOffhandItem();
    ItemStack sourceStack = sourceStackRef.copy();
    if (sourceStack.isEmpty()) {
      return false;
    }

    BlockPos placementPos = supportPos.relative(clickedFace);
    BlockState beforeState = world.getBlockState(placementPos);
    ItemStack previousOffhand = player.getOffhandItem().copy();

    inventory.setItem(slot, ItemStack.EMPTY);
    player.setItemInHand(InteractionHand.OFF_HAND, sourceStack);

    try {
      // Aim at support face center so vanilla placement logic gets realistic hit context.
      Vec3 hitVec = Vec3.atCenterOf(supportPos).add(
          clickedFace.getStepX() * 0.5D,
          clickedFace.getStepY() * 0.5D,
          clickedFace.getStepZ() * 0.5D);
      BlockHitResult hitResult = new BlockHitResult(hitVec, clickedFace, supportPos, false);
      InteractionResult result = useItemOnAsPlayer(InteractionHand.OFF_HAND, hitResult);
      BlockState afterState = world.getBlockState(placementPos);
      boolean placed = !afterState.equals(beforeState);
      if (placed) {
        SoundType soundType = afterState.getSoundType();
        world.playSound(
            null,
            placementPos,
            soundType.getPlaceSound(),
            SoundSource.BLOCKS,
            (soundType.getVolume() + 1.0F) / 2.0F,
            soundType.getPitch() * 0.8F);
      }
      return result.consumesAction() || placed;
    } finally {
      // Always restore inventory/offhand even if placement call path throws.
      ItemStack updatedOffhand = player.getOffhandItem().copy();
      inventory.setItem(slot, updatedOffhand);
      player.setItemInHand(InteractionHand.OFF_HAND, sourceSlotIsOffhand ? updatedOffhand.copy() : previousOffhand);
    }
  }

  /**
   * Invoke whichever useItemOn signature exists under current mappings/runtime.
   */
  private InteractionResult useItemOnAsPlayer(InteractionHand hand, BlockHitResult hitResult) {
    Method fourArgMethod = findGameModeInteractionMethod(
        ServerPlayer.class,
        Level.class,
        InteractionHand.class,
        BlockHitResult.class);
    if (fourArgMethod != null) {
      Object result = invokeGameModeMethod(fourArgMethod, player, world, hand, hitResult);
      if (result instanceof InteractionResult interactionResult) {
        return interactionResult;
      }
    }

    Method fiveArgMethod = findGameModeInteractionMethod(
        ServerPlayer.class,
        Level.class,
        ItemStack.class,
        InteractionHand.class,
        BlockHitResult.class);
    if (fiveArgMethod != null) {
      Object result = invokeGameModeMethod(fiveArgMethod, player, world, player.getItemInHand(hand), hand, hitResult);
      if (result instanceof InteractionResult interactionResult) {
        return interactionResult;
      }
    }

    if (!useItemOnLookupWarned) {
      useItemOnLookupWarned = true;
      LogUtils.logWarn(
          "Unable to resolve compatible game mode placement method for class={}; illumination placement may fail in this runtime",
          player.gameMode.getClass().getName());
    }
    return InteractionResult.PASS;
  }

  private Method findGameModeInteractionMethod(Class<?>... expectedParameterTypes) {
    Class<?> gameModeClass = player.gameMode.getClass();

    for (Method method : gameModeClass.getMethods()) {
      if (isCompatibleInteractionMethod(method, expectedParameterTypes)) {
        return method;
      }
    }

    for (Method method : gameModeClass.getDeclaredMethods()) {
      if (isCompatibleInteractionMethod(method, expectedParameterTypes)) {
        method.trySetAccessible();
        return method;
      }
    }
    return null;
  }

  private static boolean isCompatibleInteractionMethod(Method method, Class<?>... expectedParameterTypes) {
    if (method.getReturnType() != InteractionResult.class) {
      return false;
    }

    Class<?>[] actualParameterTypes = method.getParameterTypes();
    if (actualParameterTypes.length != expectedParameterTypes.length) {
      return false;
    }

    for (int i = 0; i < actualParameterTypes.length; i++) {
      // Allow supertypes in runtime signatures (for example Player instead of ServerPlayer).
      if (!actualParameterTypes[i].isAssignableFrom(expectedParameterTypes[i])) {
        return false;
      }
    }
    return true;
  }

  private Object invokeGameModeMethod(Method method, Object... args) {
    try {
      return method.invoke(player.gameMode, args);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  /**
   * Returns true if inventory still has at least one torch to place.
   */
  protected boolean playerHasTorches() {
    return findTorchSlot() >= 0;
  }

  /**
   * Break a block through player game mode while optionally forcing a tool snapshot for enchant parity.
   */
  protected BreakOutcome breakBlockWithTool(BlockPos pos, ItemStack preferredTool) {
    if (player.gameMode == null) {
      return new BreakOutcome(false, preferredTool == null ? ItemStack.EMPTY : preferredTool.copy());
    }

    BlockState beforeBreak = world.getBlockState(pos);
    int breakEffectData = Block.getId(beforeBreak);

    ItemStack configuredTool = preferredTool == null ? ItemStack.EMPTY : preferredTool;
    boolean forceTool = !configuredTool.isEmpty();
    int selectedSlotAtStart = player.getInventory().getSelectedSlot();
    ItemStack mainHandBeforeForce = player.getMainHandItem().copy();

    ItemStack effectiveTool = forceTool ? configuredTool : player.getMainHandItem();
    if (!beforeBreak.isAir()) {
      if (beforeBreak.getDestroySpeed(world, pos) < 0.0F) {
        return new BreakOutcome(false, effectiveTool.copy());
      }
      if (beforeBreak.requiresCorrectToolForDrops() && !effectiveTool.isCorrectToolForDrops(beforeBreak)) {
        return new BreakOutcome(false, effectiveTool.copy());
      }
    }

    boolean canForcePreferredTool = forceTool
        && player.getInventory().getSelectedSlot() == selectedSlotAtStart
        && ItemStack.isSameItemSameComponents(mainHandBeforeForce, configuredTool);

    if (canForcePreferredTool) {
      player.setItemInHand(InteractionHand.MAIN_HAND, configuredTool.copy());
    }

    boolean broken = player.gameMode.destroyBlock(pos);
    ItemStack usedTool = player.getMainHandItem().copy();

    if (canForcePreferredTool && player.getInventory().getSelectedSlot() == selectedSlotAtStart) {
      // Persist the post-break forced tool so durability and breakage affect the real held stack.
      player.setItemInHand(InteractionHand.MAIN_HAND, usedTool);
    }

    if (broken && !beforeBreak.isAir()) {
      // Ensure queued/programmatic breaks keep vanilla break FX feedback for nearby clients.
      world.levelEvent(2001, pos, breakEffectData);
    }

    return new BreakOutcome(broken, usedTool);
  }

  /**
   * Shared gate for veination fan-out so every mining-style agent applies the same rules and checks.
   */
  protected boolean maybeFanOutVeination(
      BlockPos pos,
      BlockState candidateState,
      boolean mineVeins,
      CommonConfig commonConfig,
      VeinationRuntimeService veinationRuntime,
      VeinationConfig veinationConfig,
      ItemStack veinationTriggerTool) {
    if (!mineVeins || veinationRuntime == null || veinationConfig == null || !veinationConfig.enabled()) {
      return false;
    }

    // Only one active VeinationAgent per player at a time; no queue pileups allowed.
    AgentManager agentManager = AgentManager.get();
    if (agentManager.hasAgentType(player, VeinationAgent.class)) {
      return true;
    }

    ItemStack toolStack = veinationTriggerTool == null || veinationTriggerTool.isEmpty()
        ? player.getMainHandItem()
        : veinationTriggerTool;

    boolean toolAllowedByAllowlist = veinationRuntime.isPickaxeAllowed(world, veinationConfig, toolStack);
    boolean toolMinesCandidate = candidateState != null
        && (!candidateState.requiresCorrectToolForDrops() || toolStack.isCorrectToolForDrops(candidateState));

    if (!toolAllowedByAllowlist && !toolMinesCandidate) {
      return false;
    }

    if (!veinationRuntime.isOreAllowed(veinationConfig, candidateState)) {
      return false;
    }

    // Anchor drops at trigger point then queue veination worker.
    veinationRuntime.registerDropAnchor(player, pos, veinationConfig);
    agentManager.addAgent(player,
        new VeinationAgent(player, pos, candidateState, commonConfig, veinationRuntime, veinationConfig, toolStack));
    return true;
  }
}
