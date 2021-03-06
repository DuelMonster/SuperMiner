package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsLumbinator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.objects.TreeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(	modid = Lumbinator.MODID,
		name = Lumbinator.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Lumbinator {
	public static final String	MODID		= "superminer_lumbinator";
	public static final String	MODName		= "Lumbinator";
	public static final String	ChannelName	= MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	// public static final int BREAK_LIMIT = 8;
	public static Globals			myGlobals			= new Globals();
	public static boolean			bShouldSyncSettings	= true;
	private static EntityPlayerMP	player				= null;
	private boolean					bHungerNotified		= false;
	private static List<SMPacket>	myPackets			= new ArrayList<SMPacket>();
	private static ItemStack		oHeldItem			= null;
	private static int				iHeldSlot			= -99;
	private static boolean			bIsChopping			= false;
	private static int				recordedXPCount		= 0;
	private static List<Entity>		recordedDrops		= Collections.synchronizedList(new ArrayList<Entity>());
	private static boolean			bIsSpawningDrops	= false;
	private static AxisAlignedBB	choppingArea		= null;
	
	public static Boolean isChopping() {
		return bIsChopping;
	}
	
	public static boolean isSpawningDrops() {
		return bIsSpawningDrops;
	}
	
	private static List<SMPacket> getMyPackets() {
		return new ArrayList<SMPacket>(myPackets);
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsLumbinator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.lumbinator.enabled"), MODID, SettingsLumbinator.bEnabledDefault, Globals.localize("superminer.lumbinator.enabled.desc"));
		SettingsLumbinator.bGatherDrops = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.lumbinator.gather_drops"), MODID, SettingsLumbinator.bGatherDropsDefault, Globals.localize("superminer.lumbinator.gather_drops.desc"));
		SettingsLumbinator.bChopTreeBelow = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.lumbinator.chop_below"), MODID, SettingsLumbinator.bChopTreeBelowDefault, Globals.localize("superminer.lumbinator.chop_below.desc"));
		SettingsLumbinator.bDestroyLeaves = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.lumbinator.destroy_leaves"), MODID, SettingsLumbinator.bDestroyLeavesDefault, Globals.localize("superminer.lumbinator.destroy_leaves.desc"));
		SettingsLumbinator.bLeavesAffectDurability = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.lumbinator.leaves_affect_durability"), MODID, SettingsLumbinator.bLeavesAffectDurabilityDefault, Globals.localize("superminer.lumbinator.leaves_affect_durability.desc"));
		SettingsLumbinator.lToolIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.lumbinator.tool_ids"), MODID, SettingsLumbinator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.tool_ids.desc"))));
		SettingsLumbinator.lWoodIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.lumbinator.wood_ids"), MODID, SettingsLumbinator.lWoodIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.wood_ids.desc"))));
		SettingsLumbinator.lLeafIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.lumbinator.leaf_ids"), MODID, SettingsLumbinator.lLeafIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.leaf_ids.desc"))));
		SettingsLumbinator.iLeafRange = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.lumbinator.leaf_range"), MODID, SettingsLumbinator.iLeafRangeDefault, 0, Integer.MAX_VALUE, Globals.localize("superminer.lumbinator.leaf_range.desc"));
		
		myGlobals.lToolIDs = Globals.IDListToArray(SettingsLumbinator.lToolIDs, false);
		myGlobals.lBlockIDs = Globals.IDListToArray(SettingsLumbinator.lWoodIDs, true);
		myGlobals.lLeafIDs = Globals.IDListToArray(SettingsLumbinator.lLeafIDs, true);
		
		List<String> order = new ArrayList<String>(9);
		order.add(Globals.localize("superminer.lumbinator.enabled"));
		order.add(Globals.localize("superminer.lumbinator.gather_drops"));
		order.add(Globals.localize("superminer.lumbinator.chop_below"));
		order.add(Globals.localize("superminer.lumbinator.destroy_leaves"));
		order.add(Globals.localize("superminer.lumbinator.leaves_affect_durability"));
		order.add(Globals.localize("superminer.lumbinator.tool_ids"));
		order.add(Globals.localize("superminer.lumbinator.wood_ids"));
		order.add(Globals.localize("superminer.lumbinator.leaf_ids"));
		order.add(Globals.localize("superminer.lumbinator.leaf_range"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings)
			bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@Mod.EventHandler
	public void imcCallback(FMLInterModComms.IMCEvent event) {
		for (final FMLInterModComms.IMCMessage message : event.getMessages())
			if (message.isStringMessage()) {
				if (message.key.equalsIgnoreCase("addAxe") && !SettingsLumbinator.lToolIDs.contains(message.getStringValue())) {
					SettingsLumbinator.lToolIDs.add(message.getStringValue());
					
					SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.lumbinator.tool_ids"), SettingsLumbinator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.tool_ids.desc")).set(SettingsLumbinator.lToolIDs.toArray(new String[0]));
					SuperMiner_Core.configFile.save();
					
				} else if (message.key.equalsIgnoreCase("addWood") && !SettingsLumbinator.lWoodIDs.contains(message.getStringValue())) {
					SettingsLumbinator.lWoodIDs.add(message.getStringValue());
					
					SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.lumbinator.wood_ids"), SettingsLumbinator.lWoodIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.wood_ids.desc")).set(SettingsLumbinator.lWoodIDs.toArray(new String[0]));
					SuperMiner_Core.configFile.save();
					
				} else if (message.key.equalsIgnoreCase("addLeaves") && !SettingsLumbinator.lLeafIDs.contains(message.getStringValue())) {
					SettingsLumbinator.lLeafIDs.add(message.getStringValue());
					
					SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.lumbinator.leaf_ids"), SettingsLumbinator.lLeafIDDefaults.toArray(new String[0]), Globals.localize("superminer.lumbinator.leaf_ids.desc")).set(SettingsLumbinator.lLeafIDs.toArray(new String[0]));
					SuperMiner_Core.configFile.save();
					
				}
			}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!SettingsLumbinator.bEnabled
				|| !PlayerEvents.IsPlayerInWorld()
				|| Excavator.isToggled()
				|| Shaftanator.bToggled
				|| !TickEvent.Phase.END.equals(event.phase))
			return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused())
			return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsLumbinator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.player;
		if (null == player)
			return;
		
		World world = mc.world;
		if (world != null) {
			IBlockState state = null;
			Block block = null;
			BlockPos oPos = null;
			
			if (player.getHealth() > 0.0F
					&& mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
				
				oPos = mc.objectMouseOver.getBlockPos();
				state = world.getBlockState(oPos);
				block = state.getBlock();
				
				if (!Globals.isAttacking(mc) && block != null && block == Blocks.AIR)
					block = null;
			}
			
			if (block != null && Globals.isIdInList(block, myGlobals.lBlockIDs)) {
				if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
					Globals.NotifyClient(Globals.tooHungry() + MODName);
					bHungerNotified = true;
					return;
				}
				
				if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) {
					myGlobals.addAttackBlock(
							player,
							state,
							oPos,
							false, true, true, false);
				}
			} else
				bHungerNotified = false;
			
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime) {
					try {
						attackPackets.remove(); // Removes packet from the history if it has been there too long.
					}
					catch (ConcurrentModificationException e) {
						SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
					}
				} else {
					block = world.getBlockState(packet.oPos).getBlock();
					if (block == null || block == Blocks.AIR) {
						try {
							attackPackets.remove(); // Removes packet from the history.
						}
						catch (ConcurrentModificationException e) {
							SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
						}
						
						Globals.sendPacket(new CPacketCustomPayload(ChannelName, packet.writePacketData()));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Lumbinator.value()) {
			SettingsLumbinator.readPacketData(payLoad);
			
			myGlobals.lToolIDs = Globals.IDListToArray(SettingsLumbinator.lToolIDs, false);
			myGlobals.lBlockIDs = Globals.IDListToArray(SettingsLumbinator.lWoodIDs, true);
			myGlobals.lLeafIDs = Globals.IDListToArray(SettingsLumbinator.lLeafIDs, true);
		} else if (SettingsLumbinator.bEnabled && iPacketID == PacketIDs.BLOCKINFO.value()) {
			SMPacket packet = new SMPacket();
			packet.readPacketData(payLoad);
			
			if (player == null)
				player = ((NetHandlerPlayServer) event.getHandler()).player;
			
			if (isChoppableTree(packet)) {
				myPackets.add(packet);
				chopTree(packet);
			}
		}
	}
	
	private static boolean isChoppableTree(SMPacket currentPacket) {
		Block block = currentPacket.block;
		IBlockState state = block.getBlockState().getBaseState();
		if (block == null || state.getMaterial() != Material.WOOD || !Globals.isIdInList(block, myGlobals.lBlockIDs))
			return false;
		
		ItemStack oEquippedItem = player.getHeldItemMainhand();
		if (oEquippedItem == null
				|| oEquippedItem.getCount() <= 0
				|| !Globals.isIdInList(oEquippedItem.getItem(), myGlobals.lToolIDs)
				|| !Globals.isIdInList(block, myGlobals.lBlockIDs))
			return false;
		
		if (!currentPacket.positions.contains(currentPacket.oPos)) {
			try {
				currentPacket.positions.offer(currentPacket.oPos);
			}
			catch (ConcurrentModificationException e) {
				SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
			}
		}
		
		// Ensure that it is a Tree ( Has Leaves )
		TreeHelper oTreeHelper = currentPacket.oTreeHelper = new TreeHelper(player, myGlobals);
		
		currentPacket.sWoodName = oTreeHelper.getUniqueIdentifier(state, currentPacket.metadata);
		// Get Leaf Name
		currentPacket.sLeafName = oTreeHelper.getLeafName(currentPacket);
		
		boolean bIsTree = false;
		
		if (!currentPacket.sLeafName.isEmpty()) {
			// Get Tree Width at breaking point
			oTreeHelper.DetectTreeSize(currentPacket);
			setChoppingArea(currentPacket);
			
			for (int yPos = (int) choppingArea.minY; yPos <= (int) choppingArea.maxY; yPos++)
				for (int xPos = (int) choppingArea.minX; xPos <= (int) choppingArea.maxX; xPos++)
					for (int zPos = (int) choppingArea.minZ; zPos <= (int) choppingArea.maxZ; zPos++)
						bIsTree = oTreeHelper.processPosition(currentPacket, new BlockPos(xPos, yPos, zPos), bIsTree);
		}
		return bIsTree;
	}
	
	@SubscribeEvent
	public void tickEvent_Server(TickEvent.ServerTickEvent event) {
		if (!isChopping() && TickEvent.Phase.END.equals(event.phase) && !myPackets.isEmpty()) {
			for (SMPacket currentPacket : getMyPackets()) {
				if (currentPacket != null)
					chopTree(currentPacket);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		
		if (getMyPackets().isEmpty() || !isChopping() || event.getWorld().isRemote || entity.isDead || event.isCanceled())
			return;
		
		if (!isSpawningDrops() && isChopping() && (entity instanceof EntityXPOrb || entity instanceof EntityItem) && Globals.isEntityWithinArea(entity, choppingArea)) {
			if (entity instanceof EntityItem) {
				this.recordDrop(entity);
				
				event.setCanceled(true);
				
			} else if (entity instanceof EntityXPOrb) {
				this.addXP(((EntityXPOrb) entity).getXpValue());
				
				event.setCanceled(true);
			}
		}
	}
	
	public void recordDrop(Entity entity) {
		synchronized (recordedDrops) {
			recordedDrops.add(entity);
		}
	}
	
	public void addXP(int value) {
		recordedXPCount += value;
	}
	
	private static void chopTree(SMPacket currentPacket) {
		bIsChopping = true;
		
		if (oHeldItem == null) {
			oHeldItem = player.getHeldItemMainhand().copy();
			iHeldSlot = player.inventory.currentItem;
		}
		
		while (!currentPacket.positions.isEmpty())
			breakBlock(currentPacket);
		
		if (currentPacket.positions.isEmpty()) {
			
			if (oHeldItem != null && player.inventory.getStackInSlot(iHeldSlot) != oHeldItem) {
				// Set current slot back to the original ItemStack
				player.inventory.setInventorySlotContents(iHeldSlot, oHeldItem);
				player.openContainer.detectAndSendChanges();
			}
			
			try {
				myPackets.remove(currentPacket);
			}
			catch (ConcurrentModificationException e) {
				SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
			}
			
			oHeldItem = null;
			iHeldSlot = -99;
		}
		bIsChopping = false;
		
		spawnDrops(currentPacket);
	}
	
	private static boolean breakBlock(SMPacket currentPacket) {
		BlockPos blockPos = null;
		
		try {
			blockPos = currentPacket.positions.poll();
		}
		catch (ConcurrentModificationException e) {
			SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
		}
		
		if (blockPos == null && !TreeHelper.ensureIsConnectedToTree(currentPacket, blockPos))
			return false;
		
		IBlockState state = player.world.getBlockState(blockPos);
		Block block = state.getBlock();
		boolean bIsLeaves = state.getMaterial() == Material.LEAVES && Globals.isIdInList(block, myGlobals.lLeafIDs);
		
		boolean bHarvested = false;
		// Double check that the block isn't air
		if (!player.world.isAirBlock(blockPos)) {
			if (bIsLeaves && !SettingsLumbinator.bLeavesAffectDurability) {
				// Set ItemStack in the current slot to null to ensure the leaves don't affect durability
				try {
					player.inventory.removeStackFromSlot(iHeldSlot);
				}
				catch (ConcurrentModificationException e) {
					SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
				}
				
				player.openContainer.detectAndSendChanges();
			}
			
			bHarvested = player.interactionManager.tryHarvestBlock(blockPos);
			
			if (oHeldItem != null && player.inventory.getStackInSlot(iHeldSlot) != oHeldItem) {
				// Set current slot back to the original ItemStack
				player.inventory.setInventorySlotContents(iHeldSlot, oHeldItem);
				player.openContainer.detectAndSendChanges();
			}
		}
		
		return bHarvested;
	}
	
	private static void setChoppingArea(SMPacket currentPacket) {
		TreeHelper oTreeHelper = currentPacket.oTreeHelper;
		
		int iMinX = (currentPacket.oPos.getX() - SettingsLumbinator.iLeafRange) - (oTreeHelper.iTreeWidthMinusX == 0 ? 1 : oTreeHelper.iTreeWidthMinusX);
		int iMaxX = (currentPacket.oPos.getX() + SettingsLumbinator.iLeafRange) + (oTreeHelper.iTreeWidthPlusX == 0 ? 1 : oTreeHelper.iTreeWidthPlusX);
		int iMinZ = (currentPacket.oPos.getZ() - SettingsLumbinator.iLeafRange) - (oTreeHelper.iTreeWidthMinusZ == 0 ? 1 : oTreeHelper.iTreeWidthMinusZ);
		int iMaxZ = (currentPacket.oPos.getZ() + SettingsLumbinator.iLeafRange) + (oTreeHelper.iTreeWidthPlusZ == 0 ? 1 : oTreeHelper.iTreeWidthPlusZ);
		int iMinY = (SettingsLumbinator.bChopTreeBelow ? oTreeHelper.iTreeMinY : currentPacket.oPos.getY());
		int iMaxY = (currentPacket.oPos.getY() + oTreeHelper.iTreeHeight);
		
		choppingArea = new AxisAlignedBB(iMinX, iMinY, iMinZ, iMaxX, iMaxY, iMaxZ);
	}
	
	public static void spawnDrops(SMPacket currentPacket) {
		bIsSpawningDrops = true;
		
		try {
			synchronized (recordedDrops) {
				while (!recordedDrops.isEmpty()) {
					List<Entity> dropsClone = new ArrayList<Entity>(recordedDrops);
					recordedDrops.clear();
					
					// Respawn the recorded Drops
					for (Entity entity : dropsClone) {
						if (entity != null && entity instanceof EntityItem && Globals.isEntityWithinArea(entity, choppingArea)) {
							if (SettingsLumbinator.bGatherDrops) {
								player.getServerWorld().spawnEntity(new EntityItem(player.getServerWorld(), currentPacket.oPos.getX(), currentPacket.oPos.getY(), currentPacket.oPos.getZ(), ((EntityItem) entity).getItem()));
							} else {
								player.getServerWorld().spawnEntity(entity);
							}
						}
					}
					
					// Respawn the recorded XP
					if (recordedXPCount > 0) {
						player.getServerWorld().spawnEntity(new EntityXPOrb(player.getServerWorld(), currentPacket.oPos.getX(), currentPacket.oPos.getY(), currentPacket.oPos.getZ(), recordedXPCount));
						recordedXPCount = 0;
					}
					
				}
			}
		}
		catch (ConcurrentModificationException e) {
			SuperMiner_Core.LOGGER.error("ConcurrentModification Exception Caught and Avoided : " + Globals.getStackTrace());
		}
		
		bIsSpawningDrops = false;
	}
}
