package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsCaptivator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(	modid = Captivator.MODID,
		name = Captivator.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Captivator {
	public static final String	MODID	= "superminer_captivator";
	public static final String	MODName	= "Captivator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	private long packetEnableTime = System.currentTimeMillis() + Globals.packetWaitMilliSec;
	
	public static boolean		bShouldSyncSettings	= true;
	private static List<Object>	lItemIDs			= null;
	private EntityPlayerMP		player				= null;
	private List<Entity>		recordedDrops		= Collections.synchronizedList(new ArrayList<Entity>());
	private boolean				bShouldCapture		= false;
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsCaptivator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.captivator.enabled"), MODID, SettingsCaptivator.bEnabledDefault, Globals.localize("superminer.captivator.enabled.desc"));
		SettingsCaptivator.bAllowInGUI = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.captivator.allow_in_gui"), MODID, SettingsCaptivator.bAllowInGUIDefault, Globals.localize("superminer.captivator.allow_in_gui.desc"));
		SettingsCaptivator.fHorizontal = SuperMiner_Core.configFile.getFloat(Globals.localize("superminer.captivator.h_radius"), MODID, SettingsCaptivator.fHorizontalDefault, 0.0F, 128.0F, Globals.localize("superminer.captivator.h_radius.desc"));
		SettingsCaptivator.fVertical = SuperMiner_Core.configFile.getFloat(Globals.localize("superminer.captivator.v_radius"), MODID, SettingsCaptivator.fVerticalDefault, 0.0F, 128.0F, Globals.localize("superminer.captivator.v_radius.desc"));
		SettingsCaptivator.bIsWhitelist = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.captivator.whitelist"), MODID, SettingsCaptivator.bIsWhitelistDefault, Globals.localize("superminer.captivator.whitelist.desc"));
		SettingsCaptivator.lItemIDs = SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.captivator.item_ids"), MODID, SettingsCaptivator.lItemIDDefaults, Globals.localize("superminer.captivator.item_ids.desc"));
		
		lItemIDs = Globals.IDListToArray(SettingsCaptivator.lItemIDs, false);
		
		List<String> order = new ArrayList<String>(6);
		order.add(Globals.localize("superminer.captivator.enabled"));
		order.add(Globals.localize("superminer.captivator.allow_in_gui"));
		order.add(Globals.localize("superminer.captivator.h_radius"));
		order.add(Globals.localize("superminer.captivator.v_radius"));
		order.add(Globals.localize("superminer.captivator.whitelist"));
		order.add(Globals.localize("superminer.captivator.item_ids"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings)
			bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!SettingsCaptivator.bEnabled || !PlayerEvents.IsPlayerInWorld() || !TickEvent.Phase.END.equals(event.phase))
			return;
		
		if (Excavator.isExcavating() || Excavator.isSpawningDrops()
				|| Shaftanator.isExcavating() || Shaftanator.isSpawningDrops()
				|| Veinator.isExcavating() || Veinator.isSpawningDrops())
			return;
		
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		if (minecraft.player == null || minecraft.world == null || minecraft.isGamePaused())
			return;
		if (!SettingsCaptivator.bAllowInGUI && !minecraft.inGameHasFocus)
			return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsCaptivator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = minecraft.player;
		if (null == player || player.isDead || player.isPlayerSleeping())
			return;
		
		World world = minecraft.world;
		if (null != world && System.currentTimeMillis() >= this.packetEnableTime && player.getHealth() > 0.0F) {
			
			List<Entity> list = Globals.getNearbyEntities(world, player.getEntityBoundingBox().grow(SettingsCaptivator.fHorizontal, SettingsCaptivator.fVertical, SettingsCaptivator.fHorizontal));
			if (null == list || list.isEmpty())
				return;
			
			if (System.currentTimeMillis() >= this.packetEnableTime) {
				this.packetEnableTime = (System.currentTimeMillis() + Globals.packetWaitMilliSec);
				
				PacketBuffer packetData = new PacketBuffer(Unpooled.buffer());
				packetData.writeInt(PacketIDs.BLOCKINFO.value());
				
				Globals.sendPacket(new CPacketCustomPayload(ChannelName, packetData));
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (!SettingsCaptivator.bEnabled
				|| event.getWorld().isRemote || event.getEntity().isDead || event.isCanceled()
				|| Excavator.isExcavating() || Lumbinator.isChopping() || Shaftanator.isExcavating() || Veinator.isExcavating())
			return;
		
		if ((event.getEntity() instanceof EntityXPOrb || event.getEntity() instanceof EntityItem)) {
			synchronized (this.recordedDrops) {
				this.recordedDrops.add(0, event.getEntity());
			}
		}
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Captivator.value())
			SettingsCaptivator.readPacketData(payLoad);
		
		else if (SettingsCaptivator.bEnabled && !bShouldCapture) {
			player = ((NetHandlerPlayServer) event.getHandler()).player;
			if (null == player)
				return;
			bShouldCapture = true;
		}
	}
	
	@SubscribeEvent
	public void tickEvent_World(TickEvent.WorldTickEvent event) {
		if (!SettingsCaptivator.bEnabled || !bShouldCapture || TickEvent.Phase.END.equals(event.phase))
			return;
		
		if (Excavator.isExcavating() || Excavator.isSpawningDrops()
				|| Shaftanator.isExcavating() || Shaftanator.isSpawningDrops()
				|| Veinator.isExcavating() || Veinator.isSpawningDrops())
			return;
		
		synchronized (this.recordedDrops) {
			AxisAlignedBB captivateArea = player.getEntityBoundingBox().grow(SettingsCaptivator.fHorizontal, SettingsCaptivator.fVertical, SettingsCaptivator.fHorizontal);
			
			if (player.world != null) {
				List<Entity> tmpDrops = Globals.getNearbyEntities(player.world, captivateArea);
				if (tmpDrops != null && !tmpDrops.isEmpty()) {
					for (Entity entity : tmpDrops) {
						if (this.recordedDrops.isEmpty() || !this.recordedDrops.contains(entity))
							this.recordedDrops.add(entity);
					}
				}
				
				if (this.recordedDrops.isEmpty())
					return;
				tmpDrops = new ArrayList<Entity>(this.recordedDrops);
				for (Entity entity : tmpDrops) {
					
					if (entity != null && !entity.isDead && Globals.isEntityWithinArea(entity, captivateArea)) {
						if (entity instanceof EntityXPOrb) {
							
							entity.onCollideWithPlayer(player);
							this.recordedDrops.remove(entity);
							
						} else if (entity instanceof EntityItem) { // && Globals.hasInventoryGotSpace(player,
																	// ((EntityItem) entity).getEntityItem())) {
							EntityItem eItem = (EntityItem) entity;
							
							if (!eItem.cannotPickup()
									&& (lItemIDs == null || lItemIDs.isEmpty() || Globals.isIdInList(eItem.getItem().getItem(), lItemIDs) == SettingsCaptivator.bIsWhitelist)) {
								
								eItem.onCollideWithPlayer(player);
								
								if (eItem.isDead)
									this.recordedDrops.remove(eItem);
							}
						}
					} else
						this.recordedDrops.remove(entity);
				}
			}
		}
		bShouldCapture = false;
	}
}
