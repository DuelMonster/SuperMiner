package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsSubstitutor;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.objects.SubstitutionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(	modid = Substitutor.MODID,
		name = Substitutor.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Substitutor {
	public static final String	MODID	= "superminer_substitutor";
	public static final String	MODName	= "Substitutor";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	public static FMLEventChannel eventChannel;
	
	public static List<Object> lExcludedBlockIDs = null;
	
	public static boolean bShouldSyncSettings = true;
	
	private static boolean	wasAttacking	= false;
	private static boolean	bSwitchback		= true;
	private static int		iSubstitueTool	= -99;
	private static int		iPrevItem		= -99;
	
	private EntityLivingBase entityAttacking = null;
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsSubstitutor.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.substitutor.enabled"), MODID, SettingsSubstitutor.bEnabledDefault, Globals.localize("superminer.substitutor.enabled.desc"));
		SettingsSubstitutor.bSwitchbackEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.substitutor.switchback"), MODID, SettingsSubstitutor.bSwitchbackEnabledDefault, Globals.localize("superminer.substitutor.switchback.desc"));
		SettingsSubstitutor.bFavourSilkTouch = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.substitutor.silk"), MODID, SettingsSubstitutor.bFavourSilkTouchDefault, Globals.localize("superminer.substitutor.silk.desc"));
		SettingsSubstitutor.bIgnoreIfValidTool = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.substitutor.ignore_valid"), MODID, SettingsSubstitutor.bIgnoreIfValidToolDefault, Globals.localize("superminer.substitutor.ignore_valid.desc"));
		SettingsSubstitutor.bIgnorePassiveMobs = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.substitutor.ignore_passive"), MODID, SettingsSubstitutor.bIgnorePassiveMobsDefault, Globals.localize("superminer.substitutor.ignore_passive.desc"));
		SettingsSubstitutor.saExcludedBlockIDs = SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.substitutor.excluded_ids"), MODID, SettingsSubstitutor.saExcludedBlockIDDefaults, Globals.localize("superminer.substitutor.excluded_ids.desc"));
		
		lExcludedBlockIDs = Globals.IDListToArray(SettingsSubstitutor.saExcludedBlockIDs, true);
		
		List<String> order = new ArrayList<String>(6);
		order.add(Globals.localize("superminer.substitutor.enabled"));
		order.add(Globals.localize("superminer.substitutor.switchback"));
		order.add(Globals.localize("superminer.substitutor.silk"));
		order.add(Globals.localize("superminer.substitutor.ignore_valid"));
		order.add(Globals.localize("superminer.substitutor.ignore_passive"));
		order.add(Globals.localize("superminer.substitutor.excluded_ids"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		PacketBuffer payLoadClone = new PacketBuffer(payLoad.copy());
		int iPacketID = payLoadClone.readInt();
		
		if (iPacketID == PacketIDs.Settings_Substitutor.value()) {
			SettingsSubstitutor.readPacketData(payLoad);
			
			lExcludedBlockIDs = Globals.IDListToArray(SettingsSubstitutor.saExcludedBlockIDs, true);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() ||
				!SettingsSubstitutor.bEnabled ||
				!TickEvent.Phase.START.equals(event.phase) ||
				Excavator.isExcavating() ||
				Illuminator.isPlacingTorch() ||
				Shaftanator.isExcavating() ||
				Veinator.isExcavating()) return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused() || mc.playerController.isInCreativeMode()) return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsSubstitutor.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.player;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;
		
		World world = mc.world;
		if (null == world) return;
		
		boolean isAttacking = Globals.isAttacking(mc);
		if (!isAttacking && wasAttacking && bSwitchback && player.inventory.currentItem != iPrevItem) {
			if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Restoring item from {" + player.inventory.currentItem + "} to {" + iPrevItem + "}");
			player.inventory.currentItem = iPrevItem;
			iSubstitueTool = -99;
			iPrevItem = -99;
			bSwitchback = false;
		} else if (isAttacking && !wasAttacking)
			iPrevItem = player.inventory.currentItem;
		
		if (isAttacking && mc.objectMouseOver != null)
			if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos oPos = mc.objectMouseOver.getBlockPos();
			Block block = world.getBlockState(oPos).getBlock();
			
			if (!Globals.isIdInList(block, lExcludedBlockIDs))
				SubstitueTool(world, oPos);
			} else if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && mc.objectMouseOver.entityHit instanceof EntityLivingBase)
				SubstitueWeapon((EntityLivingBase) mc.objectMouseOver.entityHit);
			
		wasAttacking = isAttacking;
	}
	
	private void SubstitueTool(World world, BlockPos oPos) {
		try {
			iSubstitueTool = iPrevItem;
			Minecraft mc = FMLClientHandler.instance().getClient();
			NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
			
			// Abort Substitution if the current Item is valid for the attacking block
			/*
			 * if (SettingsSubstitutor.bIgnoreIfValidTool && mc.player.getHeldItemMainhand() != null &&
			 * mc.player.getHeldItemMainhand().canHarvestBlock(world.getBlockState(oPos))) return;
			 */
			
			for (int i = 0; i < 9; i++) {
				if (i == iSubstitueTool) continue;
				
				if (inventory.get(i) != null && determineTool(inventory.get(iSubstitueTool), inventory.get(i), world, oPos))
					iSubstitueTool = i;
			}
			
			if (!SettingsSubstitutor.bIgnoreIfValidTool || !SubstitutionHelper.isSameTool(inventory.get(iPrevItem), inventory.get(iSubstitueTool))) {
				if (mc.player.inventory.currentItem != iSubstitueTool) {
					if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Switching item from {" + mc.player.inventory.currentItem + "} to {" + iSubstitueTool + "}");
					mc.player.inventory.currentItem = iSubstitueTool;
					mc.player.openContainer.detectAndSendChanges();
					
					if (SettingsSubstitutor.bSwitchbackEnabled)
						bSwitchback = true;
					else
						iPrevItem = iSubstitueTool;
				}
			}
			
		} catch (Throwable e) {
			// throwException("Error switching weapons", e, false);
			System.out.println("Error switching tools - " + e.getMessage());
		}
	}
	
	private void SubstitueWeapon(EntityLivingBase entity) {
		if (SettingsSubstitutor.bIgnorePassiveMobs && !(entity instanceof EntityMob)) return;
		try {
			entityAttacking = entity;
			Minecraft mc = FMLClientHandler.instance().getClient();
			NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
			int iSubstitueWeapon = iPrevItem;
			
			for (int i = 0; i < 9; i++) {
				if (i == iSubstitueWeapon) continue;
				
				if (determineWeapon(inventory.get(iSubstitueWeapon), inventory.get(i), entity))
					iSubstitueWeapon = i;
			}
			
			if (mc.player.inventory.currentItem != iSubstitueWeapon) {
				if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Switching item from {" + mc.player.inventory.currentItem + "} to {" + iSubstitueWeapon + "}");
				mc.player.inventory.currentItem = iSubstitueWeapon;
				mc.player.openContainer.detectAndSendChanges();
				
				iPrevItem = iSubstitueWeapon;
			}
		} catch (Throwable e) {
			// throwException("Error switching weapons", e, false);
		}
	}
	
	private boolean determineTool(ItemStack CurrentTool, ItemStack CompareTool, World world, BlockPos oPos) {
		IBlockState state = world.getBlockState(oPos);
		Block block = state.getBlock();
		
		if (block == null || Blocks.AIR == block || Blocks.BEDROCK == block) return false;
		
		float currentBlockStrength = SubstitutionHelper.getBlockStrength(CurrentTool, world, oPos);
		float compareBlockStrength = SubstitutionHelper.getBlockStrength(CompareTool, world, oPos);
		
		if (currentBlockStrength <= 0F && compareBlockStrength <= 0F) return false;
		
		float currentDigSpeed = SubstitutionHelper.getDigSpeed(CurrentTool, state);
		float compareDigSpeed = SubstitutionHelper.getDigSpeed(CompareTool, state);
		
		boolean currentToolDamageable = SubstitutionHelper.isItemStackDamageable(CurrentTool);
		boolean compareToolDamageable = SubstitutionHelper.isItemStackDamageable(CompareTool);
		
		if (currentToolDamageable && !compareToolDamageable && compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed < currentDigSpeed) return false;
		
		if (SettingsSubstitutor.bFavourSilkTouch) {
			
			boolean blockIsSilkTouchable = SubstitutionHelper.isBlockSilkTouchable(world, oPos);
			boolean currentToolHasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, CurrentTool) > 0;
			boolean compareToolHasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, CompareTool) > 0;
			
			if (blockIsSilkTouchable)
				if (compareToolHasSilkTouch && !currentToolHasSilkTouch) return true;
				else if (currentToolHasSilkTouch && !compareToolHasSilkTouch) return false;
		}
		
		int currentFortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, CurrentTool);
		int compareFortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, CompareTool);
		// if (SubstitutionHelper.isBlockFortunable(world, x, y, z) && compareFortuneLevel > currentFortuneLevel) return
		// true;
		
		int currentAdjustedBlockStrength = SubstitutionHelper.getAdjustedBlockStrength(currentBlockStrength);
		int compareAdjustedBlockStrength = SubstitutionHelper.getAdjustedBlockStrength(compareBlockStrength);
		
		// Switch Tools because the Compare Tool is stronger.
		if (new Integer(compareAdjustedBlockStrength).compareTo(currentAdjustedBlockStrength) > 0) return true;
		
		Set<Enchantment> MutilToolEnchantments = SubstitutionHelper.getMultiToolEnchantments(CurrentTool, CompareTool);
		
		for (Enchantment enchantment : MutilToolEnchantments) {
			int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, CurrentTool);
			int compareLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, CompareTool);
			if (compareLevel > currentLevel) return true;
			else if (compareLevel < currentLevel) return false;
		}
		
		// boolean currentToolDamageable = SubstitutionHelper.isItemStackDamageable(CurrentTool);
		// boolean compareToolDamageable = SubstitutionHelper.isItemStackDamageable(CompareTool);
		
		// if (compareToolDamageable && !currentToolDamageable) return false;
		// else if (currentToolDamageable && !compareToolDamageable) return true;
		
		if (currentToolDamageable && compareToolDamageable) {
			if (compareFortuneLevel > currentFortuneLevel) return false;
			else if (compareFortuneLevel < currentFortuneLevel) return true;
			
			int currentUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, CurrentTool);
			int compareUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, CompareTool);
			
			if (compareUnbreakingLevel > currentUnbreakingLevel) return true;
			else if (compareUnbreakingLevel < currentUnbreakingLevel) return false;
		} else {
			int blockStrengthComparison = Float.compare(compareBlockStrength, currentBlockStrength);
			if (blockStrengthComparison > 0) return true;
			else if (blockStrengthComparison < 0) return false;
		}
		
		return false;
	}
	
	private boolean determineWeapon(ItemStack CurrentWeapon, ItemStack CompareWeapon, EntityLivingBase entity) {
		boolean isPlayer = entity instanceof EntityPlayer;
		
		double currentWeaponDamage = SubstitutionHelper.getVanillaStackDamage(CurrentWeapon, entity);
		double compareWeaponDamage = SubstitutionHelper.getVanillaStackDamage(CompareWeapon, entity);
		
		if (isPlayer) {
			if (compareWeaponDamage > currentWeaponDamage) return true;
			else if (compareWeaponDamage < currentWeaponDamage) return false;
		} else {
			
			if (SubstitutionHelper.isSword(CompareWeapon) && !SubstitutionHelper.isSword(CurrentWeapon)) return true;
			else if (!SubstitutionHelper.isSword(CompareWeapon) && SubstitutionHelper.isSword(CurrentWeapon)) return false;
			else if (!SubstitutionHelper.isSword(CompareWeapon) && !SubstitutionHelper.isSword(CurrentWeapon)) return false;
			
			int currentWeaponHits;
			int compareWeaponHits;
			
			if (currentWeaponDamage == 0) currentWeaponHits = Integer.MAX_VALUE;
			else currentWeaponHits = MathHelper.ceil(entityAttacking.getMaxHealth() / currentWeaponDamage);
			
			if (compareWeaponDamage == 0) compareWeaponHits = Integer.MAX_VALUE;
			else compareWeaponHits = MathHelper.ceil(entityAttacking.getMaxHealth() / compareWeaponDamage);
			
			if (compareWeaponHits < currentWeaponHits) return true;
			else if (compareWeaponHits > currentWeaponHits) return false;
			
		}
		
		int currentWeaponLootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, CurrentWeapon);
		int currentWeaponFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, CurrentWeapon);
		int currentWeaponKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, CurrentWeapon);
		int currentWeaponUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, CurrentWeapon);
		
		int compareWeaponLootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, CompareWeapon);
		int compareWeaponFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, CompareWeapon);
		int compareWeaponKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, CompareWeapon);
		int compareWeaponUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, CompareWeapon);
		
		if (!isPlayer) {
			if (compareWeaponLootingLevel > currentWeaponLootingLevel) return true;
			else if (compareWeaponLootingLevel < currentWeaponLootingLevel) return false;
		}
		
		if (compareWeaponFireAspectLevel > currentWeaponFireAspectLevel) return true;
		else if (compareWeaponFireAspectLevel < currentWeaponFireAspectLevel) return false;
		
		if (compareWeaponKnockbackLevel > currentWeaponKnockbackLevel) return true;
		else if (compareWeaponKnockbackLevel < currentWeaponKnockbackLevel) return false;
		
		Set<Enchantment> MutilToolEnchantments = SubstitutionHelper.getMultiToolEnchantments(CurrentWeapon, CompareWeapon);
		
		for (Enchantment enchantment : MutilToolEnchantments) {
			int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, CurrentWeapon);
			int compareLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, CompareWeapon);
			if (compareLevel > currentLevel) return true;
			else if (compareLevel < currentLevel) return false;
		}
		
		if (compareWeaponDamage > currentWeaponDamage) return true;
		else if (compareWeaponDamage < currentWeaponDamage) return false;
		
		boolean currentWeaponDamageable = SubstitutionHelper.isItemStackDamageable(CurrentWeapon);
		boolean compareWeaponDamageable = SubstitutionHelper.isItemStackDamageable(CompareWeapon);
		
		if (compareWeaponDamageable && !currentWeaponDamageable) return false;
		else if (currentWeaponDamageable && !compareWeaponDamageable) return true;
		
		if (currentWeaponDamageable && compareWeaponDamageable)
			if (compareWeaponUnbreakingLevel > currentWeaponUnbreakingLevel) return true;
			else if (compareWeaponUnbreakingLevel < currentWeaponUnbreakingLevel) return false;
			
		if (CompareWeapon == null && CurrentWeapon != null) return true;
		else if (CurrentWeapon == null && CompareWeapon != null) return false;
		
		return false;
	}
}
