package uk.co.duelmonster.minersadvantage.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.RankAndLevel;
import uk.co.duelmonster.minersadvantage.common.Ranking;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSubstituteTool;

public class SubstitutionHelper {

  private World        world     = null;
  private PlayerEntity player    = null;
  private Variables    variables = null;

  private int          optimalRankIndx = -99;
  private float        optimalDigSpeed = -99;
  private RankAndLevel optimalRank     = null;

  private List<Ranking>                  rankings   = Constants.RANKING_DEFAULT;
  private HashMap<Integer, RankAndLevel> rankingMap = new HashMap<Integer, RankAndLevel>();
  private BlockPos                       oPos       = null;
  private BlockState                     state      = null;
  private Block                          block      = null;

  public void processToolSubtitution(ServerPlayerEntity _player, BlockPos _pos) {
    this.player    = _player;
    this.world     = player.level;
    this.variables = Variables.get(player.getUUID());
    SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(player.getUUID());

    this.oPos  = _pos;
    this.state = world.getBlockState(oPos);
    this.block = state.getBlock();

    // If ignoreIfValidTool is enabled and the currently held item can break the block we cancel the
    // substitution
    if (clientConfig.substitution.ignoreIfValidTool && player.inventory.getSelected() != null && player.inventory.getSelected().isCorrectToolForDrops(this.state))
      return;

    PlayerInventory inventory = player.inventory;

    // LootContext.Builder ctx = new LootContext.Builder((ServerWorld)
    // world).withParameter(LootParameters.BLOCK_STATE,
    // state).withParameter(LootParameters.POSITION, oPos).withParameter(LootParameters.TOOL,
    // Constants.DUMMY_SILKTOUCH);
    LootContext.Builder ctx = new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.BLOCK_STATE, state).withParameter(LootParameters.ORIGIN, state.getOffset(world, oPos)).withParameter(LootParameters.TOOL, Constants.DUMMY_SILKTOUCH);

    boolean silkTouchable = state.getDrops(ctx).contains(new ItemStack(block.asItem()));

    // Rank the valid inventory slots that contain effective tools
    int iMapIndx = 0;
    for (int iPass = 1; iPass <= 2; iPass++) {
      for (int iSlot = 0; iSlot < 9; iSlot++) {
        ItemStack itemStack = inventory.getItem(iSlot);
        if (itemStack != null
            && (clientConfig.substitution.blacklist == null
                || clientConfig.substitution.blacklist.size() == 0
                || !clientConfig.substitution.blacklist.contains(Functions.getName(itemStack)))) {
          Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);

          if (iPass == 1 && silkTouchable && clientConfig.substitution.favourSilkTouch) {
            if (enchantments.containsKey(Enchantments.BLOCK_EFFICIENCY) && enchantments.containsKey(Enchantments.SILK_TOUCH)) {
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY_SILK_TOUCH, enchantments.get(Enchantments.BLOCK_EFFICIENCY)));
              iMapIndx++;
            } else if (enchantments.containsKey(Enchantments.SILK_TOUCH)) {
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.SILK_TOUCH, 1));
              iMapIndx++;
            }
          } else if (iPass == 1 && clientConfig.substitution.favourFortune) {
            if (enchantments.containsKey(Enchantments.BLOCK_EFFICIENCY) && enchantments.containsKey(Enchantments.BLOCK_FORTUNE)) {
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY_FORTUNE, enchantments.get(Enchantments.BLOCK_EFFICIENCY), enchantments.get(Enchantments.BLOCK_FORTUNE)));
              iMapIndx++;
            } else if (enchantments.containsKey(Enchantments.BLOCK_FORTUNE)) {
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.FORTUNE, enchantments.get(Enchantments.BLOCK_FORTUNE)));
              iMapIndx++;
            }
          } else {
            if (enchantments.containsKey(Enchantments.BLOCK_EFFICIENCY))
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY, enchantments.get(Enchantments.BLOCK_EFFICIENCY)));
            else if (enchantments.containsKey(Enchantments.BLOCK_FORTUNE))
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.FORTUNE, enchantments.get(Enchantments.BLOCK_FORTUNE)));
            else if (enchantments.containsKey(Enchantments.SILK_TOUCH))
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.SILK_TOUCH, 1));
            else
              rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.NONE, 0));
            iMapIndx++;
          }
        }
      }
    }

    // If there were no valid tools found then we jump out
    if (rankingMap.isEmpty())
      return;

    // Decide which is the required ranking list based on user settings
    if (silkTouchable && clientConfig.substitution.favourSilkTouch)
      rankings = Constants.RANKING_SILK_TOUCH;
    else if (clientConfig.substitution.favourFortune)
      rankings = Constants.RANKING_FORTUNE;
    else
      rankings = Constants.RANKING_DEFAULT;

    // Identify the most optimal tool for the job
    rankings.forEach(rank -> {
      rankingMap.entrySet().forEach(rankedSlot -> {

        RankAndLevel rankAndLevel      = rankedSlot.getValue();
        int          iCurrentRankIndx  = rankings.size() - rankings.indexOf(rank);
        float        iCurrentToolSpeed = getToolSpeed(inventory.getItem(rankAndLevel.SlotID));

        if (optimalDigSpeed < iCurrentToolSpeed ||
            (optimalDigSpeed <= iCurrentToolSpeed && optimalRankIndx <= iCurrentRankIndx && rankAndLevel.rank.ordinal() == rank.ordinal() &&
                (optimalRank == null ||
                    (rankAndLevel.rank.ordinal() >= optimalRank.rank.ordinal() &&
                        rankAndLevel.Level_1 > optimalRank.Level_1 &&
                        (rankAndLevel.Level_2 == -1 || rankAndLevel.Level_2 > optimalRank.Level_2))))) {
          variables.optimalSlot = rankAndLevel.SlotID;
          optimalRankIndx       = iCurrentRankIndx;
          optimalRank           = rankAndLevel;
          optimalDigSpeed       = iCurrentToolSpeed;
        }
      });
    });

    // Substitute the Players current item with the most optimal tool for the job
    if (variables.optimalSlot >= 0 && variables.optimalSlot != inventory.selected) {
      System.out.println("Switching to Optimal slot ( " + variables.optimalSlot + " )");

      variables.prevSlot          = inventory.selected;
      variables.shouldSwitchBack  = clientConfig.substitution.switchBack;
      variables.currentlySwitched = true;

      MA.NETWORK.sendTo((ServerPlayerEntity) player, new PacketSubstituteTool(variables.optimalSlot));
    }
  }

  private float getToolSpeed(ItemStack tool) {
    float f = 1.0F;

    if (!tool.isEmpty())
      f *= tool.getDestroySpeed(state);

    if (f > 1.0F) {
      int i = EnchantmentHelper.getBlockEfficiency(player);

      if (i > 0 && !tool.isEmpty()) {
        f += i * i + 1;
      }
    }

    if (player.hasEffect(Effects.MOVEMENT_SPEED))
      f *= 1.0F + (player.getEffect(Effects.MOVEMENT_SPEED).getAmplifier() + 1) * 0.2F;

    if (player.hasEffect(Effects.DIG_SLOWDOWN)) {
      float f1;

      switch (player.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) {
        case 0:
          f1 = 0.3F;
          break;
        case 1:
          f1 = 0.09F;
          break;
        case 2:
          f1 = 0.0027F;
          break;
        case 3:
        default:
          f1 = 8.1E-4F;
      }

      f *= f1;
    }

    if (player.isInWater() && !EnchantmentHelper.hasAquaAffinity(player))
      f /= 5.0F;

    if (player.isFallFlying())
      f /= 5.0F;

    f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, state, f, oPos);
    return (f < 0 ? 0 : f);
  }

  @OnlyIn(Dist.CLIENT)
  public boolean processWeaponSubtitution(ClientPlayerEntity _player, Entity target) {
    this.player    = _player;
    this.variables = Variables.get(player.getUUID());

    PlayerInventory inventory = player.inventory;

    for (int iCurrentIndx = 0; iCurrentIndx < 9; iCurrentIndx++) {
      if (iCurrentIndx == variables.optimalSlot)
        continue;

      if (variables.optimalSlot < 0 || isBestWeapon(inventory.getItem(variables.optimalSlot), inventory.getItem(iCurrentIndx), (LivingEntity) target))
        variables.optimalSlot = iCurrentIndx;
    }

    // Substitute the Players current item with the most optimal tool for the job
    if (variables.optimalSlot >= 0 && variables.optimalSlot != inventory.selected) {
      variables.shouldSwitchBack = false;

      ClientFunctions.syncCurrentPlayItem(variables.optimalSlot);

      variables.resetSubstitution();
      return true;
    }

    return false;
  }

  private boolean isBestWeapon(ItemStack currentWeapon, ItemStack compareWeapon, LivingEntity target) {

    boolean isTargetPlayer = target instanceof PlayerEntity;
    double  oldDamage      = getFullItemStackDamage(currentWeapon, target);
    double  newDamage      = getFullItemStackDamage(compareWeapon, target);

    if (isTargetPlayer) {
      return (newDamage > oldDamage);
    } else {

      int oldHits = (oldDamage == 0 ? Integer.MAX_VALUE : MathHelper.ceil(target.getMaxHealth() / oldDamage));
      int newHits = (newDamage == 0 ? Integer.MAX_VALUE : MathHelper.ceil(target.getMaxHealth() / newDamage));

      if (newHits != oldHits)
        return (newHits < oldHits);
    }

    int newLootingLevel    = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, compareWeapon);
    int newFireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, compareWeapon);
    int newKnockbackLevel  = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, compareWeapon);
    int newUnbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, compareWeapon);

    int oldLootingLevel    = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, currentWeapon);
    int oldFireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, currentWeapon);
    int oldKnockbackLevel  = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, currentWeapon);
    int oldUnbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, currentWeapon);

    if (!isTargetPlayer && newLootingLevel != oldLootingLevel)
      return (newLootingLevel > oldLootingLevel);

    if (newFireAspectLevel != oldFireAspectLevel)
      return (newFireAspectLevel > oldFireAspectLevel);

    if (newKnockbackLevel != oldKnockbackLevel)
      return (newKnockbackLevel > oldKnockbackLevel);

    Set<Enchantment> bothItemsEnchantments = getNonstandardNondamageEnchantmentsOnBothStacks(compareWeapon, currentWeapon);

    for (Enchantment enchantment : bothItemsEnchantments) {
      int oldLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, currentWeapon);
      int newLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, compareWeapon);
      if (newLevel > oldLevel) {
        return true;
      } else if (newLevel < oldLevel) {
        return false;
      }
    }

    if (isSword(compareWeapon) && !isSword(currentWeapon)) {
      return true;
    }
    if (isSword(currentWeapon) && !isSword(compareWeapon)) {
      return false;
    }

    if (newDamage > oldDamage) {
      return true;
    } else if (newDamage < oldDamage) {
      return false;
    }

    boolean newDamageable = isItemStackDamageable(compareWeapon);
    boolean oldDamageable = isItemStackDamageable(currentWeapon);

    if (newDamageable && !oldDamageable) {
      return false;
    }

    if (oldDamageable && !newDamageable) {
      return true;
    }

    if (newDamageable && oldDamageable && newUnbreakingLevel > oldUnbreakingLevel) {
      return true;
    } else if (newDamageable && oldDamageable && oldUnbreakingLevel > newUnbreakingLevel) {
      return false;
    }

    if (isItemStackEmpty(compareWeapon) && !isItemStackEmpty(currentWeapon)) {
      return true;
    } else if (isItemStackEmpty(currentWeapon) && !isItemStackEmpty(compareWeapon)) {
      return false;
    }

    return false;
  }

  private void fakeItemForPlayer(ItemStack itemstack) {
    variables.prevHeldItem = player.inventory.getItem(player.inventory.selected);
    player.inventory.setItem(player.inventory.selected, itemstack);
    if (!isItemStackEmpty(variables.prevHeldItem)) {
      player.getAttributes().removeAttributeModifiers(variables.prevHeldItem.getAttributeModifiers(EquipmentSlotType.MAINHAND));
    }
    if (!isItemStackEmpty(itemstack)) {
      player.getAttributes().addTransientAttributeModifiers(itemstack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
    }
  }

  public boolean isItemStackEmpty(ItemStack itemstack) {
    if (Constants.EMPTY_ITEMSTACK == null)
      return itemstack == null;
    return itemstack.isEmpty();
  }

  public double getFullItemStackDamage(ItemStack itemStack, LivingEntity entity) {
    fakeItemForPlayer(itemStack);
    double damage = player.getAttribute(Attributes.ATTACK_DAMAGE).getValue();

    double enchDamage = EnchantmentHelper.getDamageBonus(itemStack, entity.getMobType());

    if (damage > 0.0D || enchDamage > 0.0D) {
      boolean critical = player.fallDistance > 0.0F
          && player.isFallFlying() && !player.onClimbable()
          && !player.isInWater()
          && !player.hasEffect(Effects.BLINDNESS)
          && !player.isPassenger();

      if (critical && damage > 0) {

        damage *= 1.5D;
      }
      damage += enchDamage;
    }
    unFakeItemForPlayer();
    return damage;
  }

  public Set<Enchantment> getNonstandardNondamageEnchantmentsOnBothStacks(
      ItemStack stack1, ItemStack stack2) {

    Set<Enchantment> bothItemsEnchantments = new HashSet<Enchantment>();

    if (!isItemStackEmpty(stack1)) {
      bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
          stack1).keySet());
    }
    if (!isItemStackEmpty(stack2)) {
      bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
          stack2).keySet());
    }

    Iterator<Enchantment> iterator = bothItemsEnchantments.iterator();
    while (iterator.hasNext()) {
      Enchantment enchantment = iterator.next();
      if (enchantment == null) {
        iterator.remove();
        continue;
      }
      ResourceLocation location = enchantment.getRegistryName();
      if (location == null || location.toString().toLowerCase().startsWith("minecraft")) {
        iterator.remove();
      }
    }

    return bothItemsEnchantments;
  }

  public boolean isItemStackDamageable(ItemStack itemstack) {
    return !isItemStackEmpty(itemstack) && itemstack.isDamageableItem();
  }

  public boolean isSword(ItemStack itemstack) {
    if (isItemStackEmpty(itemstack)) {
      return false;
    }
    if (itemstack.getItem() instanceof SwordItem) {
      return true;
    }

    String name = Functions.getName(itemstack);
    if (name.contains("sword")) {
      return true;
    }
    return false;
  }

  private void unFakeItemForPlayer() {
    ItemStack fakedStack = player.inventory.getItem(player.inventory.selected);
    player.inventory.setItem(player.inventory.selected, variables.prevHeldItem);
    if (!isItemStackEmpty(fakedStack)) {
      player.getAttributes().removeAttributeModifiers(fakedStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
    }
    if (!isItemStackEmpty(variables.prevHeldItem)) {
      player.getAttributes().addTransientAttributeModifiers(variables.prevHeldItem.getAttributeModifiers(EquipmentSlotType.MAINHAND));
    }
  }
}
