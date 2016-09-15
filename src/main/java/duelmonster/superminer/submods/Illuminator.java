package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsIlluminator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.network.packets.IlluminatorPacket;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.objects.Globals;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
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
		
		MinecraftForge.EVENT_BUS.register(this);
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
			
			int x = (int)(player.getEntityBoundingBox().minX + 0.5F);
			int y = (int)player.getEntityBoundingBox().minY;
			int z = (int)(player.getEntityBoundingBox().minZ + 0.5F);
			
			BlockPos oPos = new BlockPos(x, y, z);
			IBlockState state = world.getBlockState(oPos.down());
			
			if (!world.isAirBlock(oPos)
				&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
				x = (int)(player.getEntityBoundingBox().minX - 0.5F);
				z = (int)(player.getEntityBoundingBox().minZ - 0.5F);
				
				oPos = new BlockPos(x, y, z);
				state = world.getBlockState(oPos.down());
				
				if (!world.isAirBlock(oPos)
					&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
					x = (int)(player.getEntityBoundingBox().minX + 0.5F);
					z = (int)(player.getEntityBoundingBox().minZ - 0.5F);

					oPos = new BlockPos(x, y, z);
					state = world.getBlockState(oPos.down());
					
					if (!world.isAirBlock(oPos)
						&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
						x = (int)(player.getEntityBoundingBox().minX - 0.5F);
						z = (int)(player.getEntityBoundingBox().minZ + 0.5F);

						oPos = new BlockPos(x, y, z);
						state = world.getBlockState(oPos.down());
					}
				}
			}
			
			// If the current light level is below the limit...
			if (world.getLight(oPos) <= SettingsIlluminator.iLowestLightLevel
				&& world.isAirBlock(oPos)
				&& state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {

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
			
			world.setBlockState(oPos, Blocks.torch.getDefaultState());
        	Globals.playSound(world, "dig.wood", oPos);
        	
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
				IBlockState state = world.getBlockState(oPos.down());
				
				if (!world.isAirBlock(oPos)
					&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
					x = (int)(player.getEntityBoundingBox().minX - 0.5F);
					z = (int)(player.getEntityBoundingBox().minZ - 0.5F);
					
					oPos = new BlockPos(x, y, z);
					state = world.getBlockState(oPos.down());
					
					if (!world.isAirBlock(oPos)
						&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
						x = (int)(player.getEntityBoundingBox().minX + 0.5F);
						z = (int)(player.getEntityBoundingBox().minZ - 0.5F);

						oPos = new BlockPos(x, y, z);
						state = world.getBlockState(oPos.down());
						
						if (!world.isAirBlock(oPos)
							&& !state.getBlock().canPlaceTorchOnTop(world, oPos.down())) {
							x = (int)(player.getEntityBoundingBox().minX - 0.5F);
							z = (int)(player.getEntityBoundingBox().minZ + 0.5F);

							oPos = new BlockPos(x, y, z);
							state = world.getBlockState(oPos.down());
						}
					}
				}

				// Ensure the current light level is below the limit...
				if (world.getLight(oPos) <= SettingsIlluminator.iLowestLightLevel 
					&& world.isAirBlock(oPos)
					&& state.getBlock().canPlaceTorchOnTop(world, oPos.down()))
					Illuminate(player, oPos, EnumFacing.UP);
            }
	}
	
	public static void PlaceTorch(Minecraft mc, EntityPlayer player) {
		if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
			BlockPos oPos = mc.objectMouseOver.getBlockPos();
			
			if (mc.objectMouseOver.sideHit == EnumFacing.DOWN)
				return;
			else if (mc.objectMouseOver.sideHit == EnumFacing.UP)
				oPos = oPos.up();
			else if (mc.objectMouseOver.sideHit == EnumFacing.NORTH)
				oPos = oPos.north();
			else if (mc.objectMouseOver.sideHit == EnumFacing.SOUTH)
				oPos = oPos.south();
			else if (mc.objectMouseOver.sideHit == EnumFacing.EAST)
				oPos = oPos.east();
			else if (mc.objectMouseOver.sideHit == EnumFacing.WEST)
				oPos = oPos.west();

			IBlockState state = mc.theWorld.getBlockState(oPos.down());
			if (state.getBlock().canPlaceTorchOnTop(mc.theWorld, oPos.down()) ||
				state.getBlock().canPlaceBlockOnSide(mc.theWorld, oPos, mc.objectMouseOver.sideHit))
				Illuminate(player, oPos, mc.objectMouseOver.sideHit);
		}
	}
}
