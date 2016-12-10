package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsExcavator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.ExcavationHelper;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.objects.GodItems;
import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.EnumFacing;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

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

		FMLCommonHandler.instance().bus().register(this);
	}
	
	public static void syncConfig() {
		SettingsExcavator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.enabled"), MODID, SettingsExcavator.bEnabledDefault, Globals.localize("superminer.excavator.enabled.desc"));
		SettingsExcavator.bGatherDrops = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.gather_drops"), MODID, SettingsExcavator.bGatherDropsDefault, Globals.localize("superminer.excavator.gather_drops.desc"));
		SettingsExcavator.bAutoIlluminate = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.auto_illum"), MODID, SettingsExcavator.bAutoIlluminateDefault, Globals.localize("superminer.excavator.auto_illum.desc"));
		// SettingsExcavator.bMineVeins = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.excavator.mine_veins"), MODID, SettingsExcavator.bMineVeinsDefault, Globals.localize("superminer.excavator.mine_veins.desc"));
		SettingsExcavator.lToolIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.excavator.tool_ids"), MODID, SettingsExcavator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.excavator.tool_ids.desc"))));
		SettingsExcavator.iBlockRadius = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.radius"), MODID, SettingsExcavator.iBlockRadiusDefault, SettingsExcavator.MIN_BlockRadius, SettingsExcavator.MAX_BlockRadius, Globals.localize("superminer.excavator.radius.desc"));
		SettingsExcavator.iBlockLimit = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.excavator.limit"), MODID, SettingsExcavator.iBlockLimitDefault, ((SettingsExcavator.MIN_BlockRadius * SettingsExcavator.MIN_BlockRadius) * SettingsExcavator.MIN_BlockRadius), ((SettingsExcavator.MAX_BlockRadius * SettingsExcavator.MAX_BlockRadius) * SettingsExcavator.MAX_BlockRadius), Globals.localize("superminer.excavator.limit.desc"));

		myGlobals.lToolIDs = Globals.IDListToArray(SettingsExcavator.lToolIDs, false);
		myGlobals.iBlockRadius = SettingsExcavator.iBlockRadius;
		myGlobals.iBlockLimit = SettingsExcavator.iBlockLimit;

		List<String> order = new ArrayList<String>(7);
		order.add(Globals.localize("superminer.excavator.enabled"));
		order.add(Globals.localize("superminer.excavator.gather_drops"));
		order.add(Globals.localize("superminer.excavator.auto_illum"));
		//order.add(Globals.localize("superminer.excavator.mine_veins"));
		order.add(Globals.localize("superminer.excavator.tool_ids"));
		order.add(Globals.localize("superminer.excavator.radius"));
		order.add(Globals.localize("superminer.excavator.limit"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for(final FMLInterModComms.IMCMessage message : event.getMessages())
        	if (message.isStringMessage()) {
        		
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
			Globals.sendPacket(new C17PacketCustomPayload(ChannelName, SettingsExcavator.writePacketData()));
			bShouldSyncSettings = false;
		}

		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;
		
		World world = mc.theWorld;
		if (world != null) {
			
			bToggled = Globals.isKeyDownEx(KeyBindings.excavator_toggle.getKeyCode());
			bLayerOnlyToggled = Globals.isKeyDownEx(KeyBindings.excavator_layer_only_toggle.getKeyCode());

			GodItems.isWorthy(bToggled);
			
			Block block = null;
			int metadata = -1;
			BlockPos oPos = null;
					
			if (isToggled()
				&& (Globals.isAttacking(mc) || Globals.isUsingItem(mc))
				&& player.getHealth() > 0.0F && mc.objectMouseOver != null
				&& mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {

				oPos = new BlockPos(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
				block = world.getBlock(oPos.getX(), oPos.getY(), oPos.getZ());
				metadata = world.getBlockMetadata(oPos.getX(), oPos.getY(), oPos.getZ());
				
				if(!Globals.isAttacking(mc) && block != null && block == Blocks.air)
					block = null;
				
				if (block != null) {
	
					if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
						Globals.NotifyClient(Globals.tooHungry() + MODName);
						bHungerNotified = true;
						return;
					}
					
					if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) {
						myGlobals.addAttackBlock(player,
												 block, metadata, oPos,
												 EnumFacing.getFront(mc.objectMouseOver.sideHit),
												 false, false, false,
												 bLayerOnlyToggled);
					}
				} else bHungerNotified = false;
			}
			
			// Removes packets from the history.
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = (SMPacket)attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime)
					attackPackets.remove(); // Removes packet from the history if it has been there too long.
				else {
					block = world.getBlock(packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ());
					if (block == null || block == Blocks.air) {
						attackPackets.remove(); // Removes packet from the history.
						packet.block = packet.prevBlock;
						packet.bLayerOnlyToggled = bLayerOnlyToggled;
								
						Globals.sendPacket(new C17PacketCustomPayload(ChannelName, packet.writePacketData()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.packet.payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Excavator.value()) {
			SettingsExcavator.readPacketData(payLoad);

			myGlobals.lToolIDs = Globals.IDListToArray(SettingsExcavator.lToolIDs, false);
			myGlobals.iBlockRadius = SettingsExcavator.iBlockRadius;
			myGlobals.iBlockLimit = SettingsExcavator.iBlockLimit;

		} else if (SettingsExcavator.bEnabled) {
			EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
			
			if (iPacketID == PacketIDs.BLOCKINFO.value()) {
				
				SMPacket packet = new SMPacket();
				packet.readPacketData(payLoad);
				Excavate(packet, player);
				
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
		if (null == block || Blocks.air == block || block.getMaterial().isLiquid() || Blocks.bedrock == block) return false;
		return player.canHarvestBlock(block);
	}
}
