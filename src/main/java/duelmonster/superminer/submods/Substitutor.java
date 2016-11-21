package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsSubstitutor;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.objects.SubstitutionHelper;
import duelmonster.superminer.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

@Mod(	modid = Substitutor.MODID
	  , name = Substitutor.MODName
	  , version = SuperMiner_Core.VERSION
	  , acceptedMinecraftVersions = SuperMiner_Core.MCVERSION
)
public class Substitutor {
	public static final String MODID = "superminer_substitutor";
	public static final String MODName = "Substitutor";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	public static FMLEventChannel eventChannel;
	
	public static List<Object> lExcludedBlockIDs = null;

	public static boolean bShouldSyncSettings = true;

	private static boolean wasAttacking = false;
	private static boolean bSwitchback = true;
	private static int iSubstitueTool = -99;
	private static int iPrevItem = -99;
	
	private static final int ATTACKSTAGE_NONE		= 0;
	private static final int ATTACKSTAGE_ATTACKING	= 1;
	private static final int ATTACKSTAGE_STOPPED	= 2;

	private int attackStage = ATTACKSTAGE_NONE;
	private EntityLivingBase entityAttacking = null;
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		FMLCommonHandler.instance().bus().register(this);
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
		PacketBuffer payLoad = new PacketBuffer(event.packet.payload());
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
				Veinator.isMiningVein()) return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused() || mc.playerController.isInCreativeMode()) return;

		if (bShouldSyncSettings) {
			Globals.sendPacket(new C17PacketCustomPayload(ChannelName, SettingsSubstitutor.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;

		World world = mc.theWorld;
		if (null == world) return;
		
//		if (attackStage == ATTACKSTAGE_STOPPED) {
//			//mc.thePlayer.swingArm();
//			mc.playerController.attackEntity(mc.thePlayer, entityAttacking);
//			attackStage = ATTACKSTAGE_NONE;
//			entityAttacking = null;
//			return;
//		}
		
		boolean isAttacking = Globals.isAttacking(mc); 
		if (!isAttacking && wasAttacking && bSwitchback && player.inventory.currentItem != iPrevItem) {
			if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Restoring item from {" + player.inventory.currentItem + "} to {" + iPrevItem + "}");
			player.inventory.currentItem = iPrevItem;
			iSubstitueTool = -99;
			iPrevItem = -99;
			bSwitchback = false;
		}
		else if (isAttacking && !wasAttacking)
			iPrevItem = player.inventory.currentItem;
		
		if (isAttacking && mc.objectMouseOver != null)
			if (mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
	            int x = mc.objectMouseOver.blockX;
	            int y = mc.objectMouseOver.blockY;
	            int z = mc.objectMouseOver.blockZ;

	    		Block block = world.getBlock(x, y, z);

	    		if (!Globals.isIdInList(block, lExcludedBlockIDs))
	    			SubstitueTool(world, new BlockPos(x, y, z));
			}
			else if (mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY && mc.objectMouseOver.entityHit instanceof EntityLivingBase)
				SubstitueWeapon((EntityLivingBase)mc.objectMouseOver.entityHit);
		
		wasAttacking = isAttacking;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onEntityAttack(AttackEntityEvent event) {
		if (!event.entity.worldObj.isRemote) return;
		
		if (attackStage == ATTACKSTAGE_ATTACKING && entityAttacking == event.target) {
			attackStage = ATTACKSTAGE_STOPPED;
			event.setCanceled(true);
		} else if (attackStage != ATTACKSTAGE_STOPPED) {
			entityAttacking = null;
			attackStage = ATTACKSTAGE_NONE;
		}
	}
	
	private void SubstitueTool(World world, BlockPos oPos) {
		try {
			iSubstitueTool = iPrevItem;
			Minecraft mc = FMLClientHandler.instance().getClient();
			ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
			
			// Abort Substitution if the current Item is valid for the attacking block
			/*if (SettingsSubstitutor.bIgnoreIfValidTool
				&& mc.thePlayer.getCurrentEquippedItem().getItem().canHarvestBlock(world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ()), null))
				return;*/

			for (int i = 0; i < 9; i++) {
				if (i == iSubstitueTool) continue;
				
				if (inventory[i] != null && determineTool(inventory[iSubstitueTool], inventory[i], world, oPos))
					iSubstitueTool = i;
			}
	
			if (!SettingsSubstitutor.bIgnoreIfValidTool || !SubstitutionHelper.isSameTool(inventory[iPrevItem], inventory[iSubstitueTool])) {
				if (mc.thePlayer.inventory.currentItem != iSubstitueTool) {
					if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Switching item from {" + mc.thePlayer.inventory.currentItem + "} to {" + iSubstitueTool + "}");
					mc.thePlayer.inventory.currentItem = iSubstitueTool;
					mc.thePlayer.openContainer.detectAndSendChanges();

					if (SettingsSubstitutor.bSwitchbackEnabled)
						bSwitchback = true;
					else
						iPrevItem = iSubstitueTool;
				}
			}
			
		} catch (Throwable e) {
			//throwException("Error switching weapons", e, false);
			System.out.println("Error switching tools - " + e.getMessage());
		}
	}
	
	private void SubstitueWeapon(EntityLivingBase entity) {
		if (SettingsSubstitutor.bIgnorePassiveMobs && !(entity instanceof EntityMob)) return;
		try {
			entityAttacking = entity;
			attackStage = ATTACKSTAGE_ATTACKING;
			Minecraft mc = FMLClientHandler.instance().getClient();
			ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
			int iSubstitueWeapon = iPrevItem;

			for (int i = 0; i < 9; i++) {
				if (i == iSubstitueWeapon) continue;

				if (determineWeapon(inventory[iSubstitueWeapon], inventory[i], entity))
					iSubstitueWeapon = i;
			}

			if (mc.thePlayer.inventory.currentItem != iSubstitueWeapon) {
				if (System.getProperty("DEBUG") != null) Globals.NotifyClient(" " + MODName + " = Switching item from {" + mc.thePlayer.inventory.currentItem + "} to {" + iSubstitueWeapon + "}");
				mc.thePlayer.inventory.currentItem = iSubstitueWeapon;
				mc.thePlayer.openContainer.detectAndSendChanges();
				
//				if (SettingsSubstitutor.bSwitchbackEnabled)
//					bSwitchback = true;
//				else
				iPrevItem = iSubstitueWeapon;
			}
		} catch (Throwable e) {
			//throwException("Error switching weapons", e, false);
		}
	}

	private boolean determineTool(ItemStack CurrentTool, ItemStack CompareTool, World world, BlockPos oPos) {
		Block block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());

		if (block == null || Blocks.air == block || Blocks.bedrock == block) return false;
		
		float currentBlockStrength = SubstitutionHelper.getBlockStrength(CurrentTool, world, oPos);
		float compareBlockStrength = SubstitutionHelper.getBlockStrength(CompareTool, world, oPos);
		
		if (currentBlockStrength <= 0F && compareBlockStrength <= 0F) return false;

		int metadata = world.getBlockMetadata(oPos.getX(), oPos.getY(), oPos.getZ());
		float currentDigSpeed = SubstitutionHelper.getDigSpeed(CurrentTool, block, metadata);
		float compareDigSpeed = SubstitutionHelper.getDigSpeed(CompareTool, block, metadata);
		
		boolean currentToolDamageable = SubstitutionHelper.isItemStackDamageable(CurrentTool);
		boolean compareToolDamageable = SubstitutionHelper.isItemStackDamageable(CompareTool);
		
		if (currentToolDamageable && !compareToolDamageable && compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed < currentDigSpeed) return false;

		if (SettingsSubstitutor.bFavourSilkTouch) {
			
			boolean blockIsSilkTouchable = SubstitutionHelper.isBlockSilkTouchable(world, oPos);
			boolean currentToolHasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, CurrentTool) > 0;
			boolean compareToolHasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, CompareTool) > 0;
			
			if (blockIsSilkTouchable)
				if (compareToolHasSilkTouch && !currentToolHasSilkTouch) return true;
				else if (currentToolHasSilkTouch && !compareToolHasSilkTouch) return false;
		}
		
		int currentFortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, CurrentTool);
		int compareFortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, CompareTool);
//		if (SubstitutionHelper.isBlockFortunable(world, x, y, z) && compareFortuneLevel > currentFortuneLevel) return true;
		
		int currentAdjustedBlockStrength = SubstitutionHelper.getAdjustedBlockStrength(currentBlockStrength);
		int compareAdjustedBlockStrength = SubstitutionHelper.getAdjustedBlockStrength(compareBlockStrength);

		// Switch Tools because the Compare Tool is stronger.
		if (new Integer(compareAdjustedBlockStrength).compareTo(currentAdjustedBlockStrength) > 0) return true;

		Set<Enchantment> MutilToolEnchantments = SubstitutionHelper.getMultiToolEnchantments(CurrentTool, CompareTool);

		for (Enchantment enchantment : MutilToolEnchantments) {
			int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, CurrentTool);
			int compareLevel = EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, CompareTool);
			if (compareLevel > currentLevel) return true;
			else if (compareLevel < currentLevel) return false;
		}

		//boolean currentToolDamageable = SubstitutionHelper.isItemStackDamageable(CurrentTool);
		//boolean compareToolDamageable = SubstitutionHelper.isItemStackDamageable(CompareTool);
		
//		if (compareToolDamageable && !currentToolDamageable) return false;
//		else if (currentToolDamageable && !compareToolDamageable) return true;

		if (currentToolDamageable && compareToolDamageable) {
			if (compareFortuneLevel > currentFortuneLevel) return false;
			else if (compareFortuneLevel < currentFortuneLevel) return true;

			int currentUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, CurrentTool);
			int compareUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, CompareTool);

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
			else currentWeaponHits = MathHelper.ceiling_double_int(entityAttacking.getMaxHealth() / currentWeaponDamage);

			if (compareWeaponDamage == 0) compareWeaponHits = Integer.MAX_VALUE;
			else compareWeaponHits = MathHelper.ceiling_double_int(entityAttacking.getMaxHealth() / compareWeaponDamage);

			if (compareWeaponHits < currentWeaponHits) return true;
			else if (compareWeaponHits > currentWeaponHits) return false;

		}

		int currentWeaponLootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, CurrentWeapon);
		int currentWeaponFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, CurrentWeapon);
		int currentWeaponKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, CurrentWeapon);
		int currentWeaponUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, CurrentWeapon);

		int compareWeaponLootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, CompareWeapon);
		int compareWeaponFireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, CompareWeapon);
		int compareWeaponKnockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, CompareWeapon);
		int compareWeaponUnbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, CompareWeapon);

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
			int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, CurrentWeapon);
			int compareLevel = EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, CompareWeapon);
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
