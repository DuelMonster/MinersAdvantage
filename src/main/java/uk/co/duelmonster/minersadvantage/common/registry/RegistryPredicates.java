package uk.co.duelmonster.minersadvantage.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Friendly pile of registry checks that answer questions like "is this block ore-ish?" without
 * forcing every caller to reinvent the same brittle string and tag logic.
 */
public final class RegistryPredicates {
  private static final int BLOCK_STATE_LOOKUP_CACHE_MAX_ENTRIES = 4096;
  private static final TagKey<Block> COMMON_ORES = blockTag("c", "ores");
  private static final TagKey<Block> NEOFORGE_ORES = blockTag("neoforge", "ores");
  private static final TagKey<Block> FORGE_ORES = blockTag("forge", "ores");
  private static final TagKey<Block> MINECRAFT_COAL_ORES = blockTag("minecraft", "coal_ores");
  private static final TagKey<Block> MINECRAFT_IRON_ORES = blockTag("minecraft", "iron_ores");
  private static final TagKey<Block> MINECRAFT_COPPER_ORES = blockTag("minecraft", "copper_ores");
  private static final TagKey<Block> MINECRAFT_GOLD_ORES = blockTag("minecraft", "gold_ores");
  private static final TagKey<Block> MINECRAFT_REDSTONE_ORES = blockTag("minecraft", "redstone_ores");
  private static final TagKey<Block> MINECRAFT_EMERALD_ORES = blockTag("minecraft", "emerald_ores");
  private static final TagKey<Block> MINECRAFT_LAPIS_ORES = blockTag("minecraft", "lapis_ores");
  private static final TagKey<Block> MINECRAFT_DIAMOND_ORES = blockTag("minecraft", "diamond_ores");
  private static final Map<String, Optional<BlockState>> BLOCK_STATE_LOOKUP_CACHE = new ConcurrentHashMap<>();

  /**
   * Utility class only; no instances, no drama, no accidental state.
   */
  private RegistryPredicates() {
  }

  /**
   * Detect pickaxes by item id suffix so this still works in runtimes where direct item-class checks
   * are less reliable than a predictable registry name.
   */
  public static boolean isPickaxeTool(ItemStack stack) {
    try {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pickaxe");
    } catch (RuntimeException | LinkageError ignored) {
      return false;
    }
  }

  /**
   * Runtime-safe axe tool check.
   */
  public static boolean isAxeTool(ItemStack stack) {
    return stack.getItem() instanceof AxeItem;
  }

  /**
   * Runtime-safe shovel tool check.
   */
  public static boolean isShovelTool(ItemStack stack) {
    return stack.getItem() instanceof ShovelItem;
  }

  /**
   * Runtime-safe hoe tool check.
   */
  public static boolean isHoeTool(ItemStack stack) {
    return stack.getItem() instanceof HoeItem;
  }

  /**
   * Identify sword-like ids using path suffix/name.
   */
  public static boolean isSwordToolId(String toolId) {
    return normalizedPath(toolId).endsWith("_sword") || normalizedPath(toolId).equals("sword");
  }

  /**
   * Identify explicit combat-mode tool ids.
   */
  public static boolean isCombatToolId(String toolId) {
    return normalizedPath(toolId).contains("combat");
  }

  /**
   * Determine ore-like blocks using vanilla ore tags plus ancient debris.
   */
  public static boolean isOreLike(BlockState state) {
    if (state.is(COMMON_ORES) || state.is(NEOFORGE_ORES) || state.is(FORGE_ORES)) {
      return true;
    }

    if (state.is(MINECRAFT_COAL_ORES)
        || state.is(MINECRAFT_IRON_ORES)
        || state.is(MINECRAFT_COPPER_ORES)
        || state.is(MINECRAFT_GOLD_ORES)
        || state.is(MINECRAFT_REDSTONE_ORES)
        || state.is(MINECRAFT_EMERALD_ORES)
        || state.is(MINECRAFT_LAPIS_ORES)
        || state.is(MINECRAFT_DIAMOND_ORES)
        || state.is(Blocks.ANCIENT_DEBRIS)) {
      return true;
    }

    String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    return path.endsWith("_ore") || path.equals("ancient_debris");
  }

  /**
   * Determine ore-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isOreLikeBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isOreLike(state);
    }

    String path = normalizedPath(blockId);
    return path.endsWith("_ore") || path.equals("ancient_debris");
  }

  /**
   * Determine stone-like blocks using stone tags and key hardcoded blocks.
   */
  public static boolean isStoneLike(BlockState state) {
    return state.is(BlockTags.BASE_STONE_OVERWORLD)
        || state.is(BlockTags.BASE_STONE_NETHER)
        || state.is(Blocks.DEEPSLATE)
        || state.is(Blocks.TUFF)
        || state.is(Blocks.CALCITE);
  }

  /**
   * Determine stone-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isStoneLikeBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isStoneLike(state);
    }

    String path = normalizedPath(blockId);
    return path.contains("stone")
        || path.contains("deepslate")
        || path.contains("tuff")
        || path.contains("calcite")
        || path.contains("netherrack")
        || path.contains("blackstone")
        || path.contains("basalt");
  }

  /**
   * Determine dirt-like blocks used by till/path workflows.
   */
  public static boolean isDirtLike(BlockState state) {
    return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH);
  }

  /**
   * Determine dirt-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isDirtLikeBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isDirtLike(state);
    }

    String path = normalizedPath(blockId);
    return path.contains("dirt")
        || path.contains("grass_block")
        || path.contains("podzol")
        || path.contains("mycelium")
        || path.contains("farmland");
  }

  /**
   * Determine whether state is a harvestable crop-style block.
   */
  public static boolean isCropBlock(BlockState state) {
    return state.getBlock() instanceof CropBlock || state.getBlock() instanceof NetherWartBlock;
  }

  /**
   * Determine crop-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isCropBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isCropBlock(state);
    }

    String path = normalizedPath(blockId);
    return path.contains("crop")
        || path.contains("wheat")
        || path.contains("carrot")
        || path.contains("potato")
        || path.contains("beetroot")
        || path.contains("nether_wart");
  }

  /**
   * Determine whether state is log-like.
   */
  public static boolean isLogLike(BlockState state) {
    return state.is(BlockTags.LOGS);
  }

  /**
   * Determine log-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isLogLikeBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isLogLike(state);
    }

    String path = normalizedPath(blockId);
    return path.endsWith("_log") || path.endsWith("_stem") || path.endsWith("_hyphae");
  }

  /**
   * Determine whether state is leaf-like (including nether wart blocks).
   */
  public static boolean isLeafLike(BlockState state) {
    return state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS);
  }

  /**
   * Determine leaf-like ids via registry resolution then heuristic fallback.
   */
  public static boolean isLeafLikeBlockId(String blockId) {
    BlockState state = resolveBlockState(blockId);
    if (state != null) {
      return isLeafLike(state);
    }

    String path = normalizedPath(blockId);
    return path.endsWith("_leaves") || path.endsWith("_wart_block");
  }

  /**
   * Resolve block id to default state using defensive registry lookup.
   */
  public static BlockState resolveBlockState(String blockId) {
    if (blockId == null || blockId.isBlank() || blockId.equals("air")) {
      return null;
    }

    Optional<BlockState> cached = BLOCK_STATE_LOOKUP_CACHE.get(blockId);
    if (cached != null) {
      return cached.orElse(null);
    }

    Optional<BlockState> resolved = resolveBlockStateUncached(blockId);
    if (BLOCK_STATE_LOOKUP_CACHE.size() >= BLOCK_STATE_LOOKUP_CACHE_MAX_ENTRIES) {
      // Keep this bounded for long-running worlds where lookups may span many transient ids.
      BLOCK_STATE_LOOKUP_CACHE.clear();
    }
    BLOCK_STATE_LOOKUP_CACHE.put(blockId, resolved);
    return resolved.orElse(null);
  }

  private static Optional<BlockState> resolveBlockStateUncached(String blockId) {

    try {
      // Yes, this is a linear scan. It's deliberate because this path is defensive lookup code,
      // not hot-loop geometry math, and we value "works everywhere" over fancy indexing here.
      for (Block block : BuiltInRegistries.BLOCK) {
        if (BuiltInRegistries.BLOCK.getKey(block).toString().equals(blockId)) {
          return Optional.of(block.defaultBlockState());
        }
      }
    } catch (RuntimeException | LinkageError ignored) {
      // Some test runtimes show up without a fully bootstrapped registry and pretend that's normal.
      // We swallow it so tests can keep moving instead of exploding over lookup plumbing.
    }
    return Optional.empty();
  }

  /**
   * Check whether id token references entity context.
   */
  public static boolean isContextEntity(String blockId) {
    return blockId != null && blockId.startsWith("entity:");
  }

  private static TagKey<Block> blockTag(String namespace, String path) {
    return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, path));
  }

  /**
   * Check whether id token references item context.
   */
  public static boolean isContextItem(String blockId) {
    return blockId != null && blockId.startsWith("item:");
  }

  /**
   * Check whether tool id indicates GUI context.
   */
  public static boolean isGUIContext(String toolId) {
    return toolId != null && toolId.contains("gui");
  }

  /**
   * Check explicit shovel-id marker used by legacy/manual paths.
   */
  public static boolean isShovelToolId(String toolId) {
    return "shovel".equals(toolId);
  }

  /**
   * Check whether block id points at sapling content.
   */
  public static boolean isSaplingBlock(String blockId) {
    return blockId != null && blockId.contains("sapling");
  }

  /**
   * Check manual-mode token presence.
   */
  public static boolean isManualToolMode(String toolHint) {
    return toolHint != null && toolHint.contains("manual");
  }

  /**
   * Check manual-left token presence.
   */
  public static boolean isManualLeftMode(String toolHint) {
    return toolHint != null && toolHint.contains("manual_left");
  }

  /**
   * Check manual-right token presence.
   */
  public static boolean isManualRightMode(String toolHint) {
    return toolHint != null && toolHint.contains("manual_right");
  }

  /**
   * Strip namespace prefix and normalize to path component.
   */
  private static String normalizedPath(String id) {
    if (id == null || id.isBlank()) {
      return "";
    }
    int separator = id.indexOf(':');
    if (separator >= 0 && separator + 1 < id.length()) {
      return id.substring(separator + 1);
    }
    return id;
  }
}
