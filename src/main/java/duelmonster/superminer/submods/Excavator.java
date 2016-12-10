package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsExcavator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.network.packets.AutoFurrowPacket;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.ExcavationHelper;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.objects.GodItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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

@Mod(	modid = Excavator.MODID
	  , name = Excavator.MODName
	  , version = SuperMiner_Core.VERSION
	  , acceptedMinecraftVersions = SuperMiner_Core.MCVERSION
)
public class Excavator {
	public static final String MODID = "superminer_excavator";
	public static final String MODName = "Excavator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));

	public static boolean bToggled = false;
	public static boolean bLayerOnlyToggled = false;
	public static boolean isToggled() { return (bToggled || bLayerOnlyToggled); }
	
	public static Globals myGlobals = new Globals();

	public static List<Object> lShovelIDs = null;

	private boolean bHungerNotified = false;
	public static boolean bShouldSyncSettings = true;

	private static List<ExcavationHelper> myExcavationHelpers = new ArrayList<ExcavationHelper>();
	private static List<ExcavationHelper> getMyExcavationHelpers() {
		return new ArrayList<ExcavationHelper>(myExcavationHelpers);
	}
	public static Boolean isExcavating() {
		boolean bIsExcavating = false;

		for (ExcavationHelper oEH : getMyExcavationHelpers())
			if (!bIsExcavating)
				bIsExcavating = (oEH != null && oEH.isExcavating());

		return bIsExcavating;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);

		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsExcavator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.enabled"), MODID, SettingsExcavator.bEnabledDefault, Globals.localize("superminer.excavator.enabled.desc"));
		SettingsExcavator.bGatherDrops = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.gather_drops"), MODID, SettingsExcavator.bGatherDropsDefault, Globals.localize("superminer.excavator.gather_drops.desc"));
		SettingsExcavator.bAutoIlluminate = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.auto_illum"), MODID, SettingsExcavator.bAutoIlluminateDefault, Globals.localize("superminer.excavator.auto_illum.desc"));
		// SettingsExcavator.bMineVeins = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.mine_veins"), MODID, SettingsExcavator.bMineVeinsDefault, Globals.localize("superminer.excavator.mine_veins.desc"));
		SettingsExcavator.bDetectVariants = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.detect_variants"), MODID, SettingsExcavator.bDetectVariantsDefault, Globals.localize("superminer.excavator.detect_variants.desc"));
		SettingsExcavator.lToolIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.excavator.tool_ids"), MODID, SettingsExcavator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.excavator.tool_ids.desc"))));
		SettingsExcavator.lShovelIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.excavator.shovel_ids"), MODID, SettingsExcavator.lShovelIDDefaults.toArray(new String[0]), Globals.localize("superminer.excavator.shovel_ids.desc"))));
		SettingsExcavator.iBlockRadius = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.radius"), MODID, SettingsExcavator.iBlockRadiusDefault, SettingsExcavator.MIN_BlockRadius, SettingsExcavator.MAX_BlockRadius, Globals.localize("superminer.excavator.radius.desc"));
		SettingsExcavator.iBlockLimit = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.limit"), MODID, SettingsExcavator.iBlockLimitDefault, ((SettingsExcavator.MIN_BlockRadius * SettingsExcavator.MIN_BlockRadius) * SettingsExcavator.MIN_BlockRadius), ((SettingsExcavator.MAX_BlockRadius * SettingsExcavator.MAX_BlockRadius) * SettingsExcavator.MAX_BlockRadius), Globals.localize("superminer.excavator.limit.desc"));
		SettingsExcavator.iPathWidth = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.path_width"), MODID, SettingsExcavator.iPathWidthDefault, 1, 16, Globals.localize("superminer.excavator.path_width.desc"));
		SettingsExcavator.iPathLength = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.path_length"), MODID, SettingsExcavator.iPathLengthDefault, 1, 64, Globals.localize("superminer.excavator.path_length.desc"));

		myGlobals.lToolIDs = Globals.IDListToArray(SettingsExcavator.lToolIDs, false);
		lShovelIDs = Globals.IDListToArray(SettingsExcavator.lShovelIDs, false);
		myGlobals.iBlockRadius = SettingsExcavator.iBlockRadius;
		myGlobals.iBlockLimit = SettingsExcavator.iBlockLimit;

		List<String> order = new ArrayList<String>(7);
		order.add(Globals.localize("superminer.excavator.enabled"));
		order.add(Globals.localize("superminer.excavator.gather_drops"));
		order.add(Globals.localize("superminer.excavator.auto_illum"));
		//order.add(Globals.localize("superminer.excavator.mine_veins"));
		order.add(Globals.localize("superminer.excavator.detect_variants"));
		order.add(Globals.localize("superminer.excavator.tool_ids"));
		order.add(Globals.localize("superminer.excavator.radius"));
		order.add(Globals.localize("superminer.excavator.limit"));
		order.add(Globals.localize("superminer.excavator.shovel_ids"));
		order.add(Globals.localize("superminer.excavator.path_width"));
		order.add(Globals.localize("superminer.excavator.path_length"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for(final FMLInterModComms.IMCMessage message : event.getMessages())
        	if (message.isStringMessage()) {
        		
        		if (message.key.equalsIgnoreCase("addShovel"))
					SettingsExcavator.lShovelIDs.add(message.getStringValue());
				
        		//else if(message.key.equalsIgnoreCase("addPickaxe"))
    			//	mySettings.lToolIDs.add(message.getStringValue());
        
        	}
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() || 
				Shaftanator.bToggled || 
				!SettingsExcavator.bEnabled || 
				!TickEvent.Phase.END.equals(event.phase)) return;

		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused()) return;

		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsExcavator.writePacketData()));
			bShouldSyncSettings = false;
		}

		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;
		
		World world = mc.theWorld;
		if (world != null) {
			
			bToggled = KeyBindings.excavator_toggle.isKeyDown();
			bLayerOnlyToggled = KeyBindings.excavator_layer_only_toggle.isKeyDown();

			GodItems.isWorthy(bToggled);
			
			IBlockState state = null;
			Block block = null;
			BlockPos oPos = null;
					
			if (isToggled()
				&& (Globals.isAttacking(mc) || Globals.isUsingItem(mc))
				&& player.getHealth() > 0.0F && mc.objectMouseOver != null
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
					
					if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) {
						if (player.getHeldItemMainhand() != null
							&& Globals.isIdInList(player.getHeldItemMainhand().getItem(), lShovelIDs)
							&& Globals.isUsingItem(mc) && (block == Blocks.GRASS || block == Blocks.DIRT)) {
							
							Globals.sendPacket(new CPacketCustomPayload(ChannelName, (new AutoFurrowPacket(PacketIDs.Excavator_ShovelPacket.value(), oPos)).writePacketData()));
						
						} else {
							myGlobals.addAttackBlock(player,
													 state, oPos,
													 mc.objectMouseOver.sideHit,
													 false, false, false,
													 bLayerOnlyToggled);
						}
					}
				} else bHungerNotified = false;
			}
			
			// Removes packets from the history.
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = (SMPacket)attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime)
					attackPackets.remove(); // Removes packet from the history if it has been there too long.
				else {
					block = world.getBlockState(packet.oPos).getBlock();
					if (block == null || block == Blocks.AIR) {
						attackPackets.remove(); // Removes packet from the history.
						packet.block = packet.prevBlock;
						packet.bLayerOnlyToggled = bLayerOnlyToggled;
								
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
		
		if (iPacketID == PacketIDs.Settings_Excavator.value()) {
			SettingsExcavator.readPacketData(payLoad);

			myGlobals.lToolIDs = Globals.IDListToArray(SettingsExcavator.lToolIDs, false);
			myGlobals.iBlockRadius = SettingsExcavator.iBlockRadius;
			myGlobals.iBlockLimit = SettingsExcavator.iBlockLimit;

		} else if (SettingsExcavator.bEnabled) {
			EntityPlayerMP player = ((NetHandlerPlayServer)event.getHandler()).playerEntity;
			
			if (iPacketID == PacketIDs.BLOCKINFO.value()) {
				
				SMPacket packet = new SMPacket();
				packet.readPacketData(payLoad);
				Excavate(packet, player);
				
			} else if (iPacketID == PacketIDs.Excavator_ShovelPacket.value()) {

				AutoFurrowPacket packet = new AutoFurrowPacket();
				packet.readPacketData(payLoad);
				LayThePath(packet, player);

			} else if (iPacketID == PacketIDs.GODITEMS.value())
				GodItems.GiveGodTools(player);
			
		}
	}
	
	protected void Excavate(SMPacket packet, EntityPlayerMP player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null || !isAllowedToMine(player, packet.block)) return;

		ExcavationHelper oEH = new ExcavationHelper(world, player, packet);
		myExcavationHelpers.add(oEH);
		oEH.getExcavationBlocks();
		if (!oEH.ExcavateSection()) {
			oEH.FinalizeExcavation();
			myExcavationHelpers.remove(oEH);
		}
	}
	
	@SubscribeEvent
	public void tickEvent_Server(TickEvent.ServerTickEvent event) {
		if (TickEvent.Phase.END.equals(event.phase) && myExcavationHelpers.size() > 0) // && this.isExcavating())
	    	for (ExcavationHelper oEH : getMyExcavationHelpers()) 
				if (oEH.isExcavating() && !oEH.ExcavateSection()) {
					oEH.FinalizeExcavation();
					myExcavationHelpers.remove(oEH);
				}
	}

	private static boolean isAllowedToMine(EntityPlayer player, Block block) {
		IBlockState state = block.getBlockState().getBaseState();
		if (null == block || Blocks.AIR == block || state.getMaterial().isLiquid() || Blocks.BEDROCK == block) return false;
		return player.canHarvestBlock(state);
	}
	
	private static void LayThePath(AutoFurrowPacket oPacket, EntityPlayer player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;

		World world = server.worldServerForDimension(player.dimension);
		if (world == null)
			return;

		getPathableLand(world, player, oPacket);

		ItemStack curItem = player.getHeldItemMainhand();

		int iPathableFound = oPacket.lstPositions.size();
		int iPathed = 0;
		while (iPathed <= iPathableFound) {
			BlockPos blockPos = oPacket.lstPositions.poll();
			if (blockPos == null)
				break;

			if (world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR) {

				Block blkPathLand = Blocks.GRASS_PATH;
				Globals.playSound(world, SoundEvents.BLOCK_GRASS_HIT, blockPos);

				world.setBlockState(blockPos, blkPathLand.getDefaultState()); // .withProperty(BlockFarmland.MOISTURE, Integer.valueOf(7)), 2);

				if (curItem != null && curItem.isItemStackDamageable())
					curItem.damageItem(1, player);

				if (curItem.getMaxDamage() <= 0) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					player.openContainer.detectAndSendChanges();
					break;
				}
			}

			iPathed++;
			if (iPathed > iPathableFound)
				break;
		}

		oPacket.lstPositions.clear();
	}

	private static void getPathableLand(World world, EntityPlayer player, AutoFurrowPacket oPacket) {
		EnumFacing facing = player.getHorizontalFacing();

		boolean bIsWidthEven = ((SettingsExcavator.iPathWidth & 1) == 0);
		int iHalfWidth = (bIsWidthEven ? SettingsExcavator.iPathWidth : SettingsExcavator.iPathWidth - 1) / 2;

		int iYOffset = 0;
		
		for (int iLengthOffset = 0; iLengthOffset < SettingsExcavator.iPathLength; iLengthOffset++)
			for (int iWidthOffset = (bIsWidthEven ? -(iHalfWidth - 1) : -iHalfWidth); iWidthOffset <= iHalfWidth; iWidthOffset++) {

				BlockPos oPos = oPacket.oPos.add(
						(facing == EnumFacing.EAST ? iLengthOffset
								: (facing == EnumFacing.WEST ? -iLengthOffset
										: (facing == EnumFacing.SOUTH ? iWidthOffset
												: (facing == EnumFacing.NORTH ? -iWidthOffset : 0)))),
						iYOffset,
						(facing == EnumFacing.SOUTH ? iLengthOffset
								: (facing == EnumFacing.NORTH ? -iLengthOffset
										: (facing == EnumFacing.EAST ? iWidthOffset
												: (facing == EnumFacing.WEST ? -iWidthOffset : 0)))));

				IBlockState state = world.getBlockState(oPos);
				Block oBlock = state.getBlock();
				
				if ((oBlock == Blocks.GRASS || oBlock == Blocks.DIRT) && 
						world.getBlockState(oPos.up()).getBlock() == Blocks.AIR) {
				
					oPacket.lstPositions.offer(oPos);
					
				} else {
					BlockPos oPosOffset = oPos.down();
					state = world.getBlockState(oPosOffset);
					oBlock = state.getBlock();
				
					if ((oBlock == Blocks.GRASS || oBlock == Blocks.DIRT) &&
							world.getBlockState(oPosOffset.up()).getBlock() == Blocks.AIR) {
						
						iYOffset = -1;
						oPacket.lstPositions.offer(oPosOffset);

					} else {
						oPosOffset = oPos.up();
						state = world.getBlockState(oPosOffset);
						oBlock = state.getBlock();
					
						if ((oBlock == Blocks.GRASS || oBlock == Blocks.DIRT) &&
								world.getBlockState(oPosOffset.up()).getBlock() == Blocks.AIR) {
								
							iYOffset = 1;
							oPacket.lstPositions.offer(oPosOffset);
						}				
					}
				}
			}
	}
}
