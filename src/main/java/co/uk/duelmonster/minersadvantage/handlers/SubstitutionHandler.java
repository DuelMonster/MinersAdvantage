package co.uk.duelmonster.minersadvantage.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.RankAndLevel;
import co.uk.duelmonster.minersadvantage.common.Ranking;
import co.uk.duelmonster.minersadvantage.common.SubstitutionHelper;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SubstitutionHandler {
	
	public static SubstitutionHandler instance = new SubstitutionHandler();
	
	private World			world		= null;
	private EntityPlayer	player		= null;
	private MAConfig		settings	= null;
	
	public boolean			bShouldSwitchBack	= false;
	public boolean			bCurrentlySwitched	= false;
	public int				iPrevSlot			= -99;
	public int				iOptimalSlot		= -99;
	private int				iOptimalRankIndx	= -99;
	private float			iOptimalDigSpeed	= -99;
	private RankAndLevel	oOptimalRank		= null;
	
	private List<Ranking>					rankings	= Constants.RANKING_DEFAULT;
	private HashMap<Integer, RankAndLevel>	rankingMap	= new HashMap<Integer, RankAndLevel>();
	private BlockPos						oPos		= null;
	private IBlockState						state		= null;
	private Block							block		= null;
	
	public void reset() {
		world = null;
		player = null;
		settings = null;
		
		bShouldSwitchBack = false;
		bCurrentlySwitched = false;
		iPrevSlot = -99;
		iOptimalSlot = -99;
		iOptimalRankIndx = -99;
		iOptimalDigSpeed = -99;
		oOptimalRank = null;
		
		rankings = Constants.RANKING_DEFAULT;
		rankingMap = new HashMap<Integer, RankAndLevel>();
		oPos = null;
		state = null;
		block = null;
	}
	
	public void processToolSubtitution(World _world, EntityPlayerSP _player, BlockPos _pos) {
		this.reset();
		
		this.world = _world;
		this.player = _player;
		this.settings = MAConfig.get();
		this.oPos = _pos;
		this.state = world.getBlockState(oPos);
		this.block = state.getBlock();
		
		InventoryPlayer inventory = player.inventory;
		
		boolean silkTouchable = block.canSilkHarvest(world, oPos, state, player);
		
		// Rank the valid inventory slots that contain effective tools
		int iMapIndx = 0;
		for (int iPass = 1; iPass <= 2; iPass++) {
			for (int iSlot = 0; iSlot < 9; iSlot++) {
				ItemStack itemStack = inventory.getStackInSlot(iSlot);
				if (itemStack != null) {// && ForgeHooks.isToolEffective(world, oPos, itemStack)) {
					Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
					
					if (iPass == 1 && silkTouchable && settings.substitution.bFavourSilkTouch()) {
						if (enchantments.containsKey(Enchantments.EFFICIENCY) && enchantments.containsKey(Enchantments.SILK_TOUCH)) {
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY_SILK_TOUCH, enchantments.get(Enchantments.EFFICIENCY)));
							iMapIndx++;
						} else if (enchantments.containsKey(Enchantments.SILK_TOUCH)) {
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.SILK_TOUCH, 1));
							iMapIndx++;
						}
					} else if (iPass == 1 && settings.substitution.bFavourFortune()) {
						if (enchantments.containsKey(Enchantments.EFFICIENCY) && enchantments.containsKey(Enchantments.FORTUNE)) {
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY_FORTUNE, enchantments.get(Enchantments.EFFICIENCY), enchantments.get(Enchantments.FORTUNE)));
							iMapIndx++;
						} else if (enchantments.containsKey(Enchantments.FORTUNE)) {
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.FORTUNE, enchantments.get(Enchantments.FORTUNE)));
							iMapIndx++;
						}
					} else {
						if (enchantments.containsKey(Enchantments.EFFICIENCY))
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.EFFICIENCY, enchantments.get(Enchantments.EFFICIENCY)));
						else if (enchantments.containsKey(Enchantments.FORTUNE))
							rankingMap.put(iMapIndx, new RankAndLevel(iSlot, Ranking.FORTUNE, enchantments.get(Enchantments.FORTUNE)));
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
		if (silkTouchable && settings.substitution.bFavourSilkTouch())
			rankings = Constants.RANKING_SILK_TOUCH;
		else if (settings.substitution.bFavourFortune())
			rankings = Constants.RANKING_FORTUNE;
		else
			rankings = Constants.RANKING_DEFAULT;
		
		// Identify the most optimal tool for the job
		rankings.forEach(rank -> {
			rankingMap.entrySet().forEach(rankedSlot -> {
				
				RankAndLevel	rankAndLevel		= rankedSlot.getValue();
				int				iCurrentRankIndx	= rankings.size() - rankings.indexOf(rank);
				float			iCurrentToolSpeed	= getToolSpeed(inventory.getStackInSlot(rankAndLevel.SlotID));
				
				if (iOptimalDigSpeed < iCurrentToolSpeed ||
						(iOptimalDigSpeed <= iCurrentToolSpeed && iOptimalRankIndx <= iCurrentRankIndx && rankAndLevel.rank.ordinal() == rank.ordinal() &&
								(oOptimalRank == null ||
										(rankAndLevel.rank.ordinal() >= oOptimalRank.rank.ordinal() &&
												rankAndLevel.Level_1 > oOptimalRank.Level_1 &&
												(rankAndLevel.Level_2 == -1 || rankAndLevel.Level_2 > oOptimalRank.Level_2))))) {
					iOptimalSlot = rankAndLevel.SlotID;
					iOptimalRankIndx = iCurrentRankIndx;
					oOptimalRank = rankAndLevel;
					iOptimalDigSpeed = iCurrentToolSpeed;
				}
			});
		});
		
		// Substitute the Players current item with the most optimal tool for the job
		if (iOptimalSlot >= 0 && iOptimalSlot != inventory.currentItem) {
			// System.out.println("Switching to Optimal slot ( " + iOptimalSlot + " )");
			
			iPrevSlot = inventory.currentItem;
			bShouldSwitchBack = settings.substitution.bSwitchBack();
			bCurrentlySwitched = true;
			inventory.currentItem = iOptimalSlot;
			ClientFunctions.syncCurrentPlayItem(iOptimalSlot);
		}
	}
	
	private float getToolSpeed(ItemStack tool) {
		float f = 1.0F;
		
		if (!tool.isEmpty())
			f *= tool.getDestroySpeed(state);
		
		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(player);
			
			if (i > 0 && !tool.isEmpty()) {
				f += i * i + 1;
			}
		}
		
		if (player.isPotionActive(MobEffects.HASTE))
			f *= 1.0F + (player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
		
		if (player.isPotionActive(MobEffects.MINING_FATIGUE)) {
			float f1;
			
			switch (player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
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
		
		if (player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(player))
			f /= 5.0F;
		
		if (!player.onGround)
			f /= 5.0F;
		
		f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, state, f, oPos);
		return (f < 0 ? 0 : f);
	}
	
	public boolean processWeaponSubtitution(EntityPlayerSP _player, Entity target) {
		reset();
		
		this.player = _player;
		this.settings = MAConfig.get();
		
		InventoryPlayer inventory = player.inventory;
		
		for (int iCurrentIndx = 0; iCurrentIndx < 9; iCurrentIndx++) {
			if (iCurrentIndx == iOptimalSlot)
				continue;
			
			if (iOptimalSlot < 0 || isBestWeapon(inventory.getStackInSlot(iOptimalSlot), inventory.getStackInSlot(iCurrentIndx), (EntityLivingBase) target))
				iOptimalSlot = iCurrentIndx;
		}
		
		// Substitute the Players current item with the most optimal tool for the job
		if (iOptimalSlot >= 0 && iOptimalSlot != inventory.currentItem) {
			bShouldSwitchBack = false;
			inventory.currentItem = iOptimalSlot;
			ClientFunctions.syncCurrentPlayItem(iOptimalSlot);
			reset();
			return true;
		}
		
		return false;
	}
	
	private boolean isBestWeapon(ItemStack currentWeapon, ItemStack compareWeapon, EntityLivingBase target) {
		
		boolean	isTargetPlayer	= target instanceof EntityPlayer;
		double	oldDamage		= SubstitutionHelper.getFullItemStackDamage(currentWeapon, target);
		double	newDamage		= SubstitutionHelper.getFullItemStackDamage(compareWeapon, target);
		
		if (isTargetPlayer) {
			return (newDamage > oldDamage);
		} else {
			
			int	oldHits	= (oldDamage == 0 ? Integer.MAX_VALUE : MathHelper.ceil(target.getMaxHealth() / oldDamage));
			int	newHits	= (newDamage == 0 ? Integer.MAX_VALUE : MathHelper.ceil(target.getMaxHealth() / newDamage));
			
			if (newHits != oldHits)
				return (newHits < oldHits);
		}
		
		int	newLootingLevel		= EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, compareWeapon);
		int	newFireAspectLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, compareWeapon);
		int	newKnockbackLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, compareWeapon);
		int	newUnbreakingLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, compareWeapon);
		
		int	oldLootingLevel		= EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, currentWeapon);
		int	oldFireAspectLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, currentWeapon);
		int	oldKnockbackLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, currentWeapon);
		int	oldUnbreakingLevel	= EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, currentWeapon);
		
		if (!isTargetPlayer && newLootingLevel != oldLootingLevel)
			return (newLootingLevel > oldLootingLevel);
		
		if (newFireAspectLevel != oldFireAspectLevel)
			return (newFireAspectLevel > oldFireAspectLevel);
		
		if (newKnockbackLevel != oldKnockbackLevel)
			return (newKnockbackLevel > oldKnockbackLevel);
		
		Set<Enchantment> bothItemsEnchantments = SubstitutionHelper.getNonstandardNondamageEnchantmentsOnBothStacks(compareWeapon, currentWeapon);
		
		for (Enchantment enchantment : bothItemsEnchantments) {
			int	oldLevel	= EnchantmentHelper.getEnchantmentLevel(enchantment, currentWeapon);
			int	newLevel	= EnchantmentHelper.getEnchantmentLevel(enchantment, compareWeapon);
			if (newLevel > oldLevel) {
				return true;
			} else if (newLevel < oldLevel) {
				return false;
			}
		}
		
		if (SubstitutionHelper.isSword(compareWeapon) && !SubstitutionHelper.isSword(currentWeapon)) {
			return true;
		}
		if (SubstitutionHelper.isSword(currentWeapon) && !SubstitutionHelper.isSword(compareWeapon)) {
			return false;
		}
		
		if (newDamage > oldDamage) {
			return true;
		} else if (newDamage < oldDamage) {
			return false;
		}
		
		boolean	newDamageable	= SubstitutionHelper.isItemStackDamageable(compareWeapon);
		boolean	oldDamageable	= SubstitutionHelper.isItemStackDamageable(currentWeapon);
		
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
		
		if (SubstitutionHelper.isItemStackEmpty(compareWeapon) && !SubstitutionHelper.isItemStackEmpty(currentWeapon)) {
			return true;
		} else if (SubstitutionHelper.isItemStackEmpty(currentWeapon) && !SubstitutionHelper.isItemStackEmpty(compareWeapon)) {
			return false;
		}
		
		return false;
	}
}
