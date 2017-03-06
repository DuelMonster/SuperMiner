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
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
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

@Mod(	modid = Illuminator.MODID,
		name = Illuminator.MODName,
		version = SuperMiner_Core.VERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION)
public class Illuminator {
	public static final String	MODID	= "superminer_illuminator";
	public static final String	MODName	= "Illuminator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
	@Mod.Instance(MODID)
	private Illuminator instance;
	
	private static BlockPos	lastTorchLocation	= null;
	public static boolean	bShouldSyncSettings	= true;
	private static boolean	bIsPlacingTorch		= false;
	
	public static boolean isPlacingTorch() {
		return bIsPlacingTorch;
	}
	
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
		
		if (!bShouldSyncSettings)
			bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() || !TickEvent.Phase.END.equals(event.phase))
			return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused())
			return;
		
		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsIlluminator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.player;
		if (null == player || player.isDead || player.isPlayerSleeping())
			return;
		
		World world = mc.world;
		if (world != null) {
			if (KeyBindings.illuminator_place.isPressed())
				Illuminator.PlaceTorch(mc, player);
			else if (bIsPlacingTorch)
				bIsPlacingTorch = false;
			else if (SettingsIlluminator.bEnabled && Globals.isAttacking(mc) && player.getHealth() > 0.0F) {
				EnumFacing sideHit = mc.objectMouseOver.sideHit;
				
				BlockPos oPos = new BlockPos(player.getPosition());
				oPos = new BlockPos(sideHit == EnumFacing.SOUTH
						? oPos.west()
						: (sideHit == EnumFacing.NORTH
								? oPos.west()
								: (sideHit == EnumFacing.EAST
										? oPos.north()
										: (sideHit == EnumFacing.WEST
												? oPos.north()
												: oPos))));
				
				IBlockState state = world.getBlockState(oPos.down());
				// If the current light level is below the limit...
				if (world.getLight(oPos) <= SettingsIlluminator.iLowestLightLevel && world.isAirBlock(oPos) && state.getBlock().canPlaceTorchOnTop(state, world, oPos.down())) {
					Globals.sendPacket(new CPacketCustomPayload(ChannelName, new IlluminatorPacket(oPos, EnumFacing.UP).writePacketData()));
				}
			}
		} else
			lastTorchLocation = null;
	}
	
	@SubscribeEvent
	public void severTickEvent(TickEvent.ServerTickEvent event) {
		if (!SettingsIlluminator.bEnabled || !TickEvent.Phase.END.equals(event.phase))
			return;
		
		// Retrieve any FMLInterModComm messages that may have been sent from other mods
		for (final FMLInterModComms.IMCMessage imcMessage : FMLInterModComms.fetchRuntimeMessages(this.instance))
			processIMC(imcMessage);
	}
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Illuminator.value())
			SettingsIlluminator.readPacketData(payLoad);
		
		else if (SettingsIlluminator.bEnabled && iPacketID == IlluminatorPacket.PACKETID_ILLUMINATOR) {
			IlluminatorPacket iPacket = new IlluminatorPacket();
			iPacket.readPacketData(payLoad);
			
			Illuminate(((NetHandlerPlayServer) event.getHandler()).playerEntity, iPacket.oPos, iPacket.sideHit);
		}
	}
	
	protected static void Illuminate(EntityPlayerMP player, BlockPos oPos, EnumFacing sideHit) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server)
			return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (world == null)
			return;
		
		int iTorchStackCount = 0;
		int iTorchIndx = -1;
		
		// Locate the players torches
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			ItemStack stack = player.inventory.mainInventory.get(i);
			if (stack != null && stack.getItem().equals(Item.getItemFromBlock(Blocks.TORCH))) {
				iTorchStackCount++;
				iTorchIndx = Globals.getSlotFromInventory(player, stack);
			}
		}
		
		if (iTorchIndx == -1) {
			for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
				ItemStack stack = player.inventory.offHandInventory.get(i);
				if (stack != null && stack.getItem().equals(Item.getItemFromBlock(Blocks.TORCH))) {
					iTorchStackCount++;
					iTorchIndx = Globals.getSlotFromInventory(player, stack);
				}
			}
		}
		
		if (iTorchIndx >= 0 && !oPos.equals(lastTorchLocation)) {
			lastTorchLocation = new BlockPos(oPos);
			
			world.setBlockState(oPos, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, sideHit));
			Globals.playSound(world, SoundEvents.BLOCK_WOOD_HIT, oPos);
			
			ItemStack torchStack = player.inventory.decrStackSize(iTorchIndx, 1);
			
			if (torchStack.getCount() <= 0) {
				lastTorchLocation = null;
				iTorchStackCount--;
			}
			
			if (iTorchStackCount == 0)
				player.sendMessage(new TextComponentString("§5[SuperMiner] §6Illuminator: §c" + Globals.localize("superminer.illuminator.no_torches")));
		}
	}
	
	public void processIMC(final FMLInterModComms.IMCMessage imcMessage) {
		if (imcMessage.key.equalsIgnoreCase("IlluminateShaft")) {
			if (imcMessage.isNBTMessage()) {
				NBTTagCompound nbt = imcMessage.getNBTValue();
				
				IlluminatorPacket iPacket = new IlluminatorPacket();
				iPacket.readPacketData(new PacketBuffer(Unpooled.copiedBuffer(nbt.getByteArray("IlluminateShaftData"))));
				
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				if (null == server)
					return;
				
				EntityPlayerMP player = (EntityPlayerMP) server.getEntityWorld().getEntityByID(iPacket.playerID);
				if (player == null)
					return;
				
				World world = server.worldServerForDimension(player.dimension);
				if (world == null)
					return;
				
				int x = (int) (iPacket.oPos.getX() + 0.5F);
				int y = iPacket.oPos.getY();
				int z = (int) (iPacket.oPos.getZ() + 0.5F);
				
				BlockPos oPos = new BlockPos(x, y, z);
				IBlockState state = world.getBlockState(oPos.down());
				
				if (!world.isAirBlock(oPos) && !state.getBlock().canPlaceTorchOnTop(state, world, oPos.down())) {
					x = (int) (player.getEntityBoundingBox().minX - 0.5F);
					z = (int) (player.getEntityBoundingBox().minZ - 0.5F);
					
					oPos = new BlockPos(x, y, z);
					state = world.getBlockState(oPos.down());
					
					if (!world.isAirBlock(oPos) && !state.getBlock().canPlaceTorchOnTop(state, world, oPos.down())) {
						x = (int) (player.getEntityBoundingBox().minX + 0.5F);
						z = (int) (player.getEntityBoundingBox().minZ - 0.5F);
						
						oPos = new BlockPos(x, y, z);
						state = world.getBlockState(oPos.down());
						
						if (!world.isAirBlock(oPos) && !state.getBlock().canPlaceTorchOnTop(state, world, oPos.down())) {
							x = (int) (player.getEntityBoundingBox().minX - 0.5F);
							z = (int) (player.getEntityBoundingBox().minZ + 0.5F);
							
							oPos = new BlockPos(x, y, z);
							state = world.getBlockState(oPos.down());
						}
					}
				}
				
				// Ensure the current light level is below the limit...
				if (world.getLight(oPos) <= SettingsIlluminator.iLowestLightLevel && world.isAirBlock(oPos) && state.getBlock().canPlaceTorchOnTop(state, world, oPos.down()))
					Illuminate(player, oPos, EnumFacing.UP);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void PlaceTorch(Minecraft mc, EntityPlayer player) {
		if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos oPos = mc.objectMouseOver.getBlockPos();
			if (mc.world.getBlockState(oPos).getBlock() != Blocks.TORCH) {
				EnumFacing sideHit = mc.objectMouseOver.sideHit;
				BlockPos oSidePos = mc.objectMouseOver.getBlockPos();
				
				if (sideHit == EnumFacing.DOWN)
					return;
				else if (sideHit == EnumFacing.UP)
					oSidePos = oPos.up();
				else if (sideHit == EnumFacing.NORTH)
					oSidePos = oPos.north();
				else if (sideHit == EnumFacing.SOUTH)
					oSidePos = oPos.south();
				else if (sideHit == EnumFacing.EAST)
					oSidePos = oPos.east();
				else if (sideHit == EnumFacing.WEST)
					oSidePos = oPos.west();
				
				if (mc.world.getBlockState(oPos).getBlock().isReplaceable(mc.world, oPos) && mc.world.getBlockState(oPos.down()).isSideSolid(mc.world, oPos.down(), EnumFacing.UP))
					Globals.sendPacket(new CPacketCustomPayload(ChannelName, (new IlluminatorPacket(oPos, EnumFacing.UP)).writePacketData()));
				else if (mc.world.isAirBlock(oSidePos) && mc.world.getBlockState(oPos).isSideSolid(mc.world, oPos, sideHit))
					Globals.sendPacket(new CPacketCustomPayload(ChannelName, (new IlluminatorPacket(oSidePos, sideHit)).writePacketData()));
			}
		}
	}
}