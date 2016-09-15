package duelmonster.superminer.submods;

import java.util.ArrayList;
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
import duelmonster.superminer.config.SettingsIlluminator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.network.packets.IlluminatorPacket;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.EnumFacing;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

@Mod(	modid = Illuminator.MODID
	  , name = Illuminator.MODName
	  , version = SuperMiner_Core.VERSION
	  , acceptedMinecraftVersions = SuperMiner_Core.MCVERSION
)
public class Illuminator {
	public static final String MODID = "superminer_illuminator";
	public static final String MODName = "Illuminator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));

    @Mod.Instance(MODID)
    private Illuminator instance;
    	
	private static BlockPos lastTorchLocation = null;
	public static boolean bShouldSyncSettings = true;
	private static boolean bIsPlacingTorch = false;
	public static boolean isPlacingTorch() { return bIsPlacingTorch; }

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public static void syncConfig() {
		SettingsIlluminator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.illuminator.enabled"), MODID, SettingsIlluminator.bEnabledDefault, Globals.localize("superminer.illuminator.enabled.desc"));
		SettingsIlluminator.iLowestLightLevel = SuperMiner_Core.configFile.getInt(Globals.localize("superminer.illuminator.light_level"), MODID, SettingsIlluminator.iLowestLightLevelDefault, 0, 16, Globals.localize("superminer.illuminator.light_level.desc"));

		List<String> order = new ArrayList<String>(2);
		order.add(Globals.localize("superminer.illuminator.enabled"));
		order.add(Globals.localize("superminer.illuminator.light_level"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);
		
		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() || 
				!SettingsIlluminator.bEnabled || 
				!TickEvent.Phase.END.equals(event.phase)) return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused()) return;

		if (bShouldSyncSettings) {
			Globals.sendPacket(new C17PacketCustomPayload(ChannelName, SettingsIlluminator.writePacketData()));
			bShouldSyncSettings = false;
		}

		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;

		World world = mc.theWorld;
		if (world != null)
			if (KeyBindings.illuminator_place.isPressed())
				Illuminator.PlaceTorch(mc, player);
			else if (bIsPlacingTorch)
				bIsPlacingTorch = false;
			else if (Globals.isAttacking(mc) && player.getHealth() > 0.0F) {
			
			int x = (int)(player.boundingBox.minX + 0.5F);
			int y = (int)player.boundingBox.minY;
			int z = (int)(player.boundingBox.minZ + 0.5F);
			
			BlockPos oPos = new BlockPos(x, y, z);
			
			if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
				|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
				x = (int)(player.boundingBox.minX - 0.5F);
				z = (int)(player.boundingBox.minZ - 0.5F);
				
				oPos = new BlockPos(x, y, z);
				
				if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
						|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
					x = (int)(player.boundingBox.minX + 0.5F);
					z = (int)(player.boundingBox.minZ - 0.5F);

					oPos = new BlockPos(x, y, z);
					
					if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
							|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
						x = (int)(player.boundingBox.minX - 0.5F);
						z = (int)(player.boundingBox.minZ + 0.5F);

						oPos = new BlockPos(x, y, z);
					}
				}
			}
			
			// If the current light level is below the limit...
			if (world.getBlockLightValue(oPos.getX(), oPos.getY(), oPos.getZ()) <= SettingsIlluminator.iLowestLightLevel
				&& world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
				&& world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {

				Globals.sendPacket(new C17PacketCustomPayload(ChannelName, new IlluminatorPacket(oPos).writePacketData()));

			}
		} else lastTorchLocation = null;
	}
	
	@SubscribeEvent
	public void severTickEvent(TickEvent.ServerTickEvent event) {
		if (!SettingsIlluminator.bEnabled || !TickEvent.Phase.END.equals(event.phase)) return;
		
		// Retrieve any FMLInterModComm messages that may have been sent from other mods
		for (final FMLInterModComms.IMCMessage imcMessage : FMLInterModComms.fetchRuntimeMessages(this.instance))
			processIMC(imcMessage);
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.packet.payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Illuminator.value())
			SettingsIlluminator.readPacketData(payLoad);
			
		else if (SettingsIlluminator.bEnabled && iPacketID == IlluminatorPacket.PACKETID_ILLUMINATOR) {
			IlluminatorPacket iPacket = new IlluminatorPacket();
			iPacket.readPacketData(payLoad);

			Illuminate(((NetHandlerPlayServer)event.handler).playerEntity, iPacket.oPos, EnumFacing.UP);
		}
	}

	protected static void Illuminate(EntityPlayer player, BlockPos oPos, EnumFacing sideHit) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null) return;

		int iTorchStackCount = 0;
		int iTorchIndx = -1;
		
		// Locate the players torches
		for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if (stack != null && stack.getItem().equals(Item.getItemFromBlock(Blocks.torch))) {
				iTorchStackCount++;
				if (iTorchIndx == -1) iTorchIndx = i;
			}
		}
		
		if (iTorchIndx >= 0 && !oPos.equals(lastTorchLocation)) {
			lastTorchLocation = new BlockPos(oPos);
			
        	world.setBlock(oPos.getX(), oPos.getY(), oPos.getZ(), Blocks.torch, 5, 2);
        	Globals.playSound(world, Blocks.torch.stepSound, oPos);
        	
        	player.inventory.mainInventory[iTorchIndx].stackSize--;
        	
			if (player.inventory.mainInventory[iTorchIndx].stackSize <= 0) {
				player.inventory.mainInventory[iTorchIndx] = null;
				lastTorchLocation = null;
				iTorchStackCount--;
				if (iTorchStackCount == 0)
					player.addChatMessage(new ChatComponentText("§5[SuperMiner] §6Illuminator: §c" + Globals.localize("superminer.illuminator.no_torches")));
			}
		}
	}
	
	public void processIMC(final FMLInterModComms.IMCMessage imcMessage) {
        if (imcMessage.key.equalsIgnoreCase("IlluminateShaft"))
            if (imcMessage.isNBTMessage()) {
            	NBTTagCompound nbt = imcMessage.getNBTValue();

				IlluminatorPacket iPacket = new IlluminatorPacket();
				iPacket.readPacketData(new PacketBuffer(Unpooled.copiedBuffer(nbt.getByteArray("IlluminateShaftData"))));
				
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				if (null == server) return;
				
				EntityPlayer player = (EntityPlayer)server.getEntityWorld().getEntityByID(iPacket.playerID);
				if (player == null) return;
				
				World world = server.worldServerForDimension(player.dimension);
				if (world == null) return;
				
				int x = (int)(iPacket.oPos.getX() + 0.5F);
				int y = (int)iPacket.oPos.getY();
				int z = (int)(iPacket.oPos.getZ() + 0.5F);
				
				BlockPos oPos = new BlockPos(x, y, z);
				
				if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
					|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
					x = (int)(iPacket.oPos.getX() - 0.5F);
					z = (int)(iPacket.oPos.getZ() - 0.5F);
					
					oPos = new BlockPos(x, y, z);
					
					if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
							|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
						x = (int)(iPacket.oPos.getX() + 0.5F);
						z = (int)(iPacket.oPos.getZ() - 0.5F);

						oPos = new BlockPos(x, y, z);
						
						if (!world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
								|| !world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ())) {
							x = (int)(iPacket.oPos.getX() - 0.5F);
							z = (int)(iPacket.oPos.getZ() + 0.5F);

							oPos = new BlockPos(x, y, z);
						}
					}
				}
				
				// Ensure the current light level is below the limit...
				if (world.getBlockLightValue(oPos.getX(), oPos.getY(), oPos.getZ()) <= SettingsIlluminator.iLowestLightLevel 
					&& world.isAirBlock(oPos.getX(), oPos.getY(), oPos.getZ())
					&& world.getBlock(oPos.getX(), oPos.down().getY(), oPos.getZ()).canPlaceTorchOnTop(world, oPos.getX(), oPos.down().getY(), oPos.getZ()))
					Illuminate(player, oPos, EnumFacing.UP);
            }
	}
	
	public static void PlaceTorch(Minecraft mc, EntityPlayer player) {
		if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
            int x = mc.objectMouseOver.blockX;
            int y = mc.objectMouseOver.blockY;
            int z = mc.objectMouseOver.blockZ;

			BlockPos oPos = new BlockPos(x, y, z);
			
			if (mc.objectMouseOver.sideHit == EnumFacing.DOWN.getIndex())
				return;
			else if (mc.objectMouseOver.sideHit == EnumFacing.UP.getIndex())
				oPos = oPos.up();
			else if (mc.objectMouseOver.sideHit == EnumFacing.NORTH.getIndex())
				oPos = oPos.north();
			else if (mc.objectMouseOver.sideHit == EnumFacing.SOUTH.getIndex())
				oPos = oPos.south();
			else if (mc.objectMouseOver.sideHit == EnumFacing.EAST.getIndex())
				oPos = oPos.east();
			else if (mc.objectMouseOver.sideHit == EnumFacing.WEST.getIndex())
				oPos = oPos.west();

			BlockPos oPosDown = oPos.down();
    		Block block = mc.theWorld.getBlock(oPosDown.getX(), oPosDown.getY(), oPosDown.getZ());
			if (block.canPlaceTorchOnTop(mc.theWorld, oPosDown.getX(), oPosDown.getY(), oPosDown.getZ()) ||
				block.canPlaceBlockOnSide(mc.theWorld, oPos.getX(), oPos.getY(), oPos.getZ(), mc.objectMouseOver.sideHit))
				Illuminate(player, oPos, EnumFacing.getFromSideHit(mc.objectMouseOver.sideHit));
		}
	}
}
