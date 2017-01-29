package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

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
import duelmonster.superminer.config.SettingsShaftanator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.ExcavationHelper;
import duelmonster.superminer.objects.Globals;
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

@Mod(	modid = Shaftanator.MODID,
		name = Shaftanator.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Shaftanator {
	public static final String	MODID	= "superminer_shaftanator";
	public static final String	MODName	= "Shaftanator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	public static Globals myGlobals = new Globals();
	
	public static boolean	bToggled			= false;
	private boolean			bHungerNotified		= false;
	public static boolean	bShouldSyncSettings	= true;
	
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
	public void init(FMLInitializationEvent evt) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public static void syncConfig() {
		SettingsShaftanator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.shaftanator.enabled"), MODID, SettingsShaftanator.bEnabledDefault, Globals.localize("superminer.shaftanator.enabled.desc"));
		SettingsShaftanator.bGatherDrops = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.shaftanator.gather_drops"), MODID, SettingsShaftanator.bGatherDropsDefault, Globals.localize("superminer.shaftanator.gather_drops.desc"));
		SettingsShaftanator.bAutoIlluminate = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.shaftanator.auto_illum"), MODID, SettingsShaftanator.bAutoIlluminateDefault, Globals.localize("superminer.shaftanator.auto_illum.desc"));
		SettingsShaftanator.bMineVeins = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.shaftanator.mine_veins"), MODID, SettingsShaftanator.bMineVeinsDefault, Globals.localize("superminer.shaftanator.mine_veins.desc"));
		SettingsShaftanator.iShaftLength = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.shaftanator.shaft_l"), MODID, SettingsShaftanator.iShaftLengthDefault, 4, 128, Globals.localize("superminer.shaftanator.shaft_l.desc"));
		SettingsShaftanator.iShaftHeight = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.shaftanator.shaft_h"), MODID, SettingsShaftanator.iShaftHeightDefault, 2, 16, Globals.localize("superminer.shaftanator.shaft_l.desc"));
		SettingsShaftanator.iShaftWidth = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.shaftanator.shaft_w"), MODID, SettingsShaftanator.iShaftWidthDefault, 1, 16, Globals.localize("superminer.shaftanator.shaft_w.desc"));
		
		List<String> order = new ArrayList<String>(7);
		order.add(Globals.localize("superminer.shaftanator.enabled"));
		order.add(Globals.localize("superminer.shaftanator.gather_drops"));
		order.add(Globals.localize("superminer.shaftanator.auto_illum"));
		order.add(Globals.localize("superminer.shaftanator.mine_veins"));
		order.add(Globals.localize("superminer.shaftanator.shaft_l"));
		order.add(Globals.localize("superminer.shaftanator.shaft_h"));
		order.add(Globals.localize("superminer.shaftanator.shaft_w"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() ||
				Excavator.isToggled() ||
				!SettingsShaftanator.bEnabled ||
				!TickEvent.Phase.END.equals(event.phase)) return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused()) return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new C17PacketCustomPayload(ChannelName, SettingsShaftanator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;
		
		World world = mc.theWorld;
		if (world != null) {
			
			bToggled = Globals.isKeyDownEx(KeyBindings.shaftanator_toggle.getKeyCode());
			
			Block block = null;
			int x = -1;
			int y = -1;
			int z = -1;
			
			if (bToggled
					&& player.getHealth() > 0.0F
					&& mc.objectMouseOver != null
					&& mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
				x = mc.objectMouseOver.blockX;
				y = mc.objectMouseOver.blockY;
				z = mc.objectMouseOver.blockZ;
				
				block = world.getBlock(x, y, z);
				
				if (!Globals.isAttacking(mc) && block != null && block == Blocks.air)
					block = null;
				
				if (block != null) {
					EnumFacing sideHit = EnumFacing.getFront(mc.objectMouseOver.sideHit);
					// Ensure the player is hitting the side of a block and not the Top or Bottom
					if (sideHit == EnumFacing.NORTH || sideHit == EnumFacing.EAST || sideHit == EnumFacing.SOUTH || sideHit == EnumFacing.WEST) {
						
						// Ensure the player is hitting a block in front of them within the Shaft Height.
						if (y >= player.boundingBox.minY && y < (player.boundingBox.minY + SettingsShaftanator.iShaftHeight)) {
							if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
								Globals.NotifyClient(Globals.tooHungry() + MODName);
								bHungerNotified = true;
								return;
							}
							
							if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) {
								int metadata = world.getBlockMetadata(x, y, z);
								myGlobals.addAttackBlock(player,
										block, metadata,
										new BlockPos(x, y, z),
										sideHit,
										false, false, false, false);
							}
						}
					}
				} else bHungerNotified = false;
			}
			
			// Removes packets from the history.
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime) {
					try {
						attackPackets.remove(); // Removes packet from the history if it has been there too long.
					} catch (ConcurrentModificationException e) {}
				} else {
					block = world.getBlock(packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ());
					if (block == null || block == Blocks.air) {
						try {
							attackPackets.remove(); // Removes packet from the history.
						} catch (ConcurrentModificationException e) {}
						
						packet.block = packet.prevBlock;
						
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
		
		if (iPacketID == PacketIDs.Settings_Shaftanator.value())
			SettingsShaftanator.readPacketData(payLoad);
		
		else if (SettingsShaftanator.bEnabled && iPacketID == PacketIDs.BLOCKINFO.value()) {
			SMPacket packet = new SMPacket();
			packet.readPacketData(payLoad);
			Perforate(packet, ((NetHandlerPlayServer) event.handler).playerEntity);
		}
	}
	
	protected void Perforate(SMPacket packet, EntityPlayerMP player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null || !isAllowedToMine(player, packet.block)) return;
		
		ExcavationHelper oEH = new ExcavationHelper(world, player, packet);
		myExcavationHelpers.add(oEH);
		oEH.getShaftBlocks();
		if (!oEH.ExcavateSection()) {
			oEH.FinalizeShaft();
			try {
				myExcavationHelpers.remove(oEH);
			} catch (ConcurrentModificationException e) {}
		}
	}
	
	@SubscribeEvent
	public void tickEvent_Server(TickEvent.ServerTickEvent event) {
		if (TickEvent.Phase.END.equals(event.phase) && myExcavationHelpers.size() > 0) { // && this.isExcavating())
			for (ExcavationHelper oEH : getMyExcavationHelpers()) {
				if (oEH.isExcavating() && !oEH.ExcavateSection()) {
					oEH.FinalizeShaft();
					try {
						myExcavationHelpers.remove(oEH);
					} catch (ConcurrentModificationException e) {}
				}
			}
		}
	}
	
	private static boolean isAllowedToMine(EntityPlayer player, Block block) {
		if (null == block || Blocks.air == block || block.getMaterial().isLiquid() || Blocks.bedrock == block) return false;
		return player.canHarvestBlock(block);
	}
}
