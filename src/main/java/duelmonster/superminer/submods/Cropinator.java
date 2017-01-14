package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsCropinator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.AutoFurrowPacket;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(	modid = Cropinator.MODID,
		name = Cropinator.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Cropinator {
	public static final String	MODID	= "superminer_cropinator";
	public static final String	MODName	= "Cropinator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	private boolean bHungerNotified = false;
	
	public static boolean bShouldSyncSettings = true;
	
	private static List<Object> lHoeIDs = null;
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsCropinator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.cropinator.enabled"), MODID, SettingsCropinator.bEnabledDefault, Globals.localize("superminer.cropinator.enabled.desc"));
		SettingsCropinator.bHarvestSeeds = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.cropinator.harvest_seeds"), MODID, SettingsCropinator.bHarvestSeedsDefault, Globals.localize("superminer.cropinator.harvest_seeds.desc"));
		SettingsCropinator.lHoeIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.cropinator.hoe_ids"), MODID, SettingsCropinator.lHoeIDDefaults.toArray(new String[0]), Globals.localize("superminer.cropinator.hoe_ids.desc"))));
		
		lHoeIDs = Globals.IDListToArray(SettingsCropinator.lHoeIDs, false);
		
		List<String> order = new ArrayList<String>(9);
		order.add(Globals.localize("superminer.cropinator.enabled"));
		order.add(Globals.localize("superminer.cropinator.harvest_seeds"));
		order.add(Globals.localize("superminer.cropinator.hoe_ids"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings)
			bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@Mod.EventHandler
	public void imcCallback(FMLInterModComms.IMCEvent event) {
		for (final FMLInterModComms.IMCMessage message : event.getMessages())
			if (message.isStringMessage()) {
				
				if (message.key.equalsIgnoreCase("addHoe") && !SettingsCropinator.lHoeIDs.contains(message.getStringValue())) {
					SettingsCropinator.lHoeIDs.add(message.getStringValue());
					
					SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.cropinator.hoe_ids"), SettingsCropinator.lHoeIDDefaults.toArray(new String[0]), Globals.localize("superminer.cropinator.hoe_ids.desc")).set(SettingsCropinator.lHoeIDs.toArray(new String[0]));
					SuperMiner_Core.configFile.save();
					
				}
			}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() ||
				!SettingsCropinator.bEnabled ||
				!TickEvent.Phase.END.equals(event.phase))
			return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (mc.player == null || mc.world == null || mc.isGamePaused() || !mc.inGameHasFocus)
			return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsCropinator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.player;
		if (null == player || player.isDead || player.isPlayerSleeping())
			return;
		
		World world = mc.world;
		if (world != null) {
			
			IBlockState state = null;
			Block block = null;
			BlockPos oPos = null;
			
			if (player.getHealth() > 0.0F && mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				
				oPos = mc.objectMouseOver.getBlockPos();
				state = world.getBlockState(oPos);
				block = state.getBlock();
				
				if (!Globals.isAttacking(mc) && block != null && block == Blocks.AIR)
					block = null;
				
				if (block != null) {
					
					if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
						Globals.NotifyClient(Globals.tooHungry() + MODName);
						bHungerNotified = true;
						return;
					}
					
					if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER
							&& player.getHeldItemMainhand() != null
							&& Globals.isIdInList(player.getHeldItemMainhand().getItem(), lHoeIDs))
						
						if (Globals.isUsingItem(mc) && (block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND))
							Globals.sendPacket(new CPacketCustomPayload(ChannelName, (new AutoFurrowPacket(PacketIDs.Cropinator_HoePacket.value(), oPos)).writePacketData()));
						
						else if (Globals.isUsingItem(mc) && block instanceof IPlantable) // state.getMaterial() ==
																							// Material.PLANTS)
							Globals.sendPacket(new CPacketCustomPayload(ChannelName, (new AutoFurrowPacket(PacketIDs.Cropinator_CropPacket.value(), oPos)).writePacketData()));
						
				} else
					bHungerNotified = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Cropinator.value())
			SettingsCropinator.readPacketData(payLoad);
		
		else if (SettingsCropinator.bEnabled) {
			EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;
			
			AutoFurrowPacket packet = new AutoFurrowPacket();
			packet.readPacketData(payLoad);
			
			if (iPacketID == PacketIDs.Cropinator_HoePacket.value())
				
				HoeTheFarm(packet, player);
			
			else if (iPacketID == PacketIDs.Cropinator_CropPacket.value())
				
				FarmTheCrops(packet, player);
		}
	}
	
	private static void HoeTheFarm(AutoFurrowPacket oPacket, EntityPlayer player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null)
			return;
		
		getFarmLand(world, oPacket);
		
		ItemStack curItem = player.getHeldItemMainhand();
		
		int iFarmableFound = oPacket.lstPositions.size();
		int iFarmed = 0;
		while (iFarmed <= iFarmableFound) {
			BlockPos blockPos = oPacket.lstPositions.poll();
			if (blockPos == null)
				break;
			
			Block blockAbove = world.getBlockState(blockPos.up()).getBlock();
			
			if (blockPos.getY() == oPacket.oPos.getY()
					&& (blockAbove == Blocks.AIR || blockAbove instanceof IPlantable)
					&& hasWaterSource(world, blockPos)) {
				
				world.setBlockState(blockPos, Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, Integer.valueOf(7)), 2);
				Globals.playSound(world, SoundEvents.ITEM_HOE_TILL, blockPos);
				
				if (blockAbove instanceof IPlantable) {
					
					int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, curItem);
					blockAbove.dropBlockAsItem(world, blockPos, world.getBlockState(blockPos.up()), fortune);
					world.setBlockState(blockPos.up(), Blocks.AIR.getDefaultState(), 2);
					
				}
				
				if (curItem != null && curItem.isItemStackDamageable())
					curItem.damageItem(1, player);
				
				if (curItem.getMaxDamage() <= 0) {
					player.inventory.removeStackFromSlot(player.inventory.currentItem);
					player.openContainer.detectAndSendChanges();
					break;
				}
			}
			
			iFarmed++;
			if (iFarmed > iFarmableFound)
				break;
		}
		
		oPacket.lstPositions.clear();
	}
	
	private static void getFarmLand(World world, AutoFurrowPacket oPacket) {
		int iRadius = 8;
		
		for (int xOffset = -iRadius; xOffset <= iRadius; ++xOffset)
			for (int zOffset = -iRadius; zOffset <= iRadius; ++zOffset) {
				BlockPos blockPos = oPacket.oPos.add(xOffset, 0, zOffset);
				Block oBlock = world.getBlockState(blockPos).getBlock();
				
				if (oBlock != null && !world.isAirBlock(blockPos)
						&& world.isAirBlock(blockPos.up())
						&& (oBlock == Blocks.GRASS || oBlock == Blocks.DIRT))
					oPacket.lstPositions.offer(blockPos);
				else
					break;
			}
	}
	
	private static boolean hasWaterSource(World world, BlockPos oPos) {
		for (int xOffset = oPos.getX() - 4; xOffset <= oPos.getX() + 4; ++xOffset)
			for (int yOffset = oPos.getY(); yOffset <= oPos.getY() + 1; ++yOffset)
				for (int zOffset = oPos.getZ() - 4; zOffset <= oPos.getZ() + 4; ++zOffset) {
					IBlockState state = world.getBlockState(new BlockPos(xOffset, yOffset, zOffset));
					if (state.getMaterial() == Material.WATER)
						return true;
				}
			
		return false;
	}
	
	private static void FarmTheCrops(AutoFurrowPacket oPacket, EntityPlayer player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null)
			return;
		
		getCrops(world, oPacket);
		
		ItemStack curItem = player.getHeldItemMainhand();
		
		int iFarmableFound = oPacket.lstPositions.size();
		int iFarmed = 0;
		while (iFarmed <= iFarmableFound) {
			BlockPos blockPos = oPacket.lstPositions.poll();
			if (blockPos == null)
				break;
			
			boolean bHarvest = true;
			IBlockState state = world.getBlockState(blockPos);
			IBlockState newState = null;
			Block blkCrop = state.getBlock();
			
			if (blkCrop instanceof BlockBeetroot && ((BlockBeetroot) blkCrop).isMaxAge(state))
				newState = blkCrop.getDefaultState().withProperty(BlockBeetroot.BEETROOT_AGE, Integer.valueOf(0));
			else if (blkCrop instanceof BlockCrops && ((BlockCrops) blkCrop).isMaxAge(state))
				newState = blkCrop.getDefaultState().withProperty(BlockCrops.AGE, Integer.valueOf(0));
			else
				bHarvest = false;
			
			if (bHarvest) {
				int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, curItem);
				
				if (SettingsCropinator.bHarvestSeeds)
					blkCrop.dropBlockAsItem(world, blockPos, state, fortune);
				else {
					List<ItemStack> drops = blkCrop.getDrops(world, blockPos, state, fortune);
					
					for (ItemStack item : drops)
						if (!(item.getItem() instanceof ItemSeeds))
							Block.spawnAsEntity(world, blockPos, item);
				}
				
				world.setBlockState(blockPos, newState, 2);
				Globals.playSound(world, SoundEvents.BLOCK_GRASS_HIT, blockPos);
				
				// Apply Item damage every 4 crops harvested. This makes item damage 1/4 per crop
				if (iFarmed > 0 && (iFarmed % 4 == 0 || iFarmed >= iFarmableFound)
						&& curItem != null && curItem.isItemStackDamageable())
					curItem.damageItem(1, player);
				
				if (curItem.getMaxDamage() <= 0) {
					player.inventory.removeStackFromSlot(player.inventory.currentItem);
					player.openContainer.detectAndSendChanges();
					break;
				}
			}
			
			iFarmed++;
			if (iFarmed > iFarmableFound)
				break;
		}
		
		oPacket.lstPositions.clear();
	}
	
	private static void getCrops(World world, AutoFurrowPacket oPacket) {
		int iPosStartEnd = 32;
		
		for (int iSpiral = 1; iSpiral <= iPosStartEnd; iSpiral++)
			for (int xOffset = -iSpiral; xOffset <= iSpiral; xOffset++)
				for (int zOffset = -iSpiral; zOffset <= iSpiral; zOffset++) {
					BlockPos blockPos = oPacket.oPos.add(xOffset, 0, zOffset);
					Block oBlock = world.getBlockState(blockPos).getBlock();
					
					if (oBlock != null &&
							oBlock != Blocks.AIR &&
							oBlock instanceof BlockCrops &&
							oPacket.isPositionConnected(blockPos))
						
						oPacket.lstPositions.offer(blockPos);
				}
	}
	
}
