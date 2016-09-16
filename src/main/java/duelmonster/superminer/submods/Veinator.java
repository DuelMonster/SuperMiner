package duelmonster.superminer.submods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsVeinator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.Globals;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.common.ForgeHooks;

@Mod(	modid = Veinator.MODID
	  , name = Veinator.MODName
	  , version = SuperMiner_Core.VERSION
	  , acceptedMinecraftVersions = SuperMiner_Core.MCVERSION
	)
public class Veinator {
	public static final String MODID = "superminer_veinator";
	public static final String MODName = "Veinator";
	
	public static final String ChannelName = MODID.substring(0, (MODID.length() < 20 ? MODID.length() : 20));
	
    @Mod.Instance(MODID)
    private Veinator instance;
    
    private static boolean bOresGot = false;

	public static Globals myGlobals = new Globals();
	
	private boolean bHungerNotified = false;
	public static boolean bShouldSyncSettings = true;
	private static boolean bMiningVein = false;
	
	public static boolean isMiningVein() { return bMiningVein; }

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(ChannelName);
		eventChannel.register(this);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void syncConfig() {
		SettingsVeinator.bEnabled = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.veinator.enabled"), MODID, SettingsVeinator.bEnabledDefault, Globals.localize("superminer.veinator.enabled.desc"));
		SettingsVeinator.bGatherDrops = SuperMiner_Core.configFile.getBoolean(Globals.localize("superminer.veinator.gather_drops"), MODID, SettingsVeinator.bGatherDropsDefault, Globals.localize("superminer.veinator.gather_drops"));
		SettingsVeinator.lToolIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.veinator.tool_ids"), MODID, SettingsVeinator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.veinator.tool_ids.desc"))));
		SettingsVeinator.lOreIDs = new ArrayList<String>(Arrays.asList(SuperMiner_Core.configFile.getStringList(Globals.localize("superminer.veinator.ore_ids"), MODID, SettingsVeinator.lOreIDDefaults.toArray(new String[0]), Globals.localize("superminer.veinator.ore_ids.desc"))));
		
		myGlobals.lToolIDs = Globals.IDListToArray(SettingsVeinator.lToolIDs, false);
		myGlobals.lBlockIDs = Globals.IDListToArray(SettingsVeinator.lOreIDs, true);

		List<String> order = new ArrayList<String>(4);
		order.add(Globals.localize("superminer.veinator.enabled"));
		order.add(Globals.localize("superminer.veinator.gather_drops"));
		order.add(Globals.localize("superminer.veinator.tool_ids"));
		order.add(Globals.localize("superminer.veinator.ore_ids"));
		
		SuperMiner_Core.configFile.setCategoryPropertyOrder(MODID, order);

		if (!bShouldSyncSettings) bShouldSyncSettings = SuperMiner_Core.configFile.hasChanged();

		if (!bOresGot) {
			bOresGot = true;
			// Check the Forge OreDictionary for any extra Ores not included in the config file.
	        String[] saOreNames = OreDictionary.getOreNames();
	        for(String sOreName : saOreNames)
	        	if (sOreName.startsWith("ore"))
		            for(ItemStack item : OreDictionary.getOres(sOreName))
		            	if(item.getItem() instanceof ItemBlock) {
			            	String sID = Item.REGISTRY.getNameForObject(item.getItem()).toString().trim();
			            	boolean bMCIncluded = sID.startsWith("minecraft:"); 
			                if ((bMCIncluded && !SettingsVeinator.lOreIDs.contains(sID.substring(10))) || (!bMCIncluded && !SettingsVeinator.lOreIDs.contains(sID))) {
			                	SettingsVeinator.lOreIDs.add(sID);
			                	
			            		SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.veinator.ore_ids"), SettingsVeinator.lOreIDDefaults.toArray(new String[0]), Globals.localize("superminer.veinator.ore_ids.desc")).set(SettingsVeinator.lOreIDs.toArray(new String[0]));
			            		SuperMiner_Core.configFile.save();
			            		
			        			try {
			        				int id = Integer.parseInt(sID.trim());
			        				myGlobals.lBlockIDs.add(Block.REGISTRY.getObjectById(id));
			        			} catch (NumberFormatException e) {
		        					myGlobals.lBlockIDs.add(Block.REGISTRY.getObject(new ResourceLocation(sID)));
			        			}
			                }
			            }
        }
	}
	
    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
    	for(final FMLInterModComms.IMCMessage message : event.getMessages())
        	if (message.isStringMessage()) {
        		
        		if(message.key.equalsIgnoreCase("addPickaxe") && !SettingsVeinator.lToolIDs.contains(message.getStringValue())) {
        			SettingsVeinator.lToolIDs.add(message.getStringValue());

            		SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.veinator.tool_ids"), SettingsVeinator.lToolIDDefaults.toArray(new String[0]), Globals.localize("superminer.veinator.tool_ids.desc")).set(SettingsVeinator.lToolIDs.toArray(new String[0]));
            		SuperMiner_Core.configFile.save();
            		
        			try {
        				int id = Integer.parseInt(message.getStringValue());
        				myGlobals.lToolIDs.add(Item.REGISTRY.getObjectById(id));
        			} catch (NumberFormatException e) {
						Item item = Item.REGISTRY.getObject(new ResourceLocation(message.getStringValue()));
						if (item != null) myGlobals.lToolIDs.add(item);
        			}
        		}
//        		else if(message.key.equalsIgnoreCase("addOre"))
//        			mySettings.lOreIDs.add(message.getStringValue());
        		
        	}
    }
    
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!PlayerEvents.IsPlayerInWorld() || 
				Excavator.isToggled() || 
				Shaftanator.bToggled || 
				!SettingsVeinator.bEnabled || 
				!TickEvent.Phase.END.equals(event.phase)) return;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (!mc.inGameHasFocus || mc.isGamePaused()) return;

		if (bShouldSyncSettings) {
			Globals.sendPacket(new CPacketCustomPayload(ChannelName, SettingsVeinator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;

		World world = mc.theWorld;
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
	    		
	            if(!Globals.isAttacking(mc) && block != null && block == Blocks.AIR)
	            	block = null;
			}
			
			if (block != null && Globals.isIdInList(block, myGlobals.lBlockIDs)) {
				if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
					Globals.NotifyClient(Globals.tooHungry() + MODName);
					bHungerNotified = true;
					return;
				}
				
				if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) 
		    		myGlobals.addAttackBlock(
		    				player,
							state, oPos,
							false, true, true, false);
				
			} else bHungerNotified = false;
			
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = (SMPacket)attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime)
					attackPackets.remove(); // Removes packet from the history if it has been there too long.
				else
				{
					block = world.getBlockState(new BlockPos(packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ())).getBlock();
					if (block == null || block == Blocks.AIR) {
						attackPackets.remove(); // Removes packet from the history.

						Globals.sendPacket(new CPacketCustomPayload(ChannelName, packet.writePacketData()));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void severTickEvent(TickEvent.ServerTickEvent event) {
		if (!SettingsVeinator.bEnabled || !TickEvent.Phase.END.equals(event.phase)) return;
		
		// Retrieve any FMLInterModComm messages that may have been sent from other mods
		for (final FMLInterModComms.IMCMessage imcMessage : FMLInterModComms.fetchRuntimeMessages(this.instance))
			processIMC(imcMessage);
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer payLoad = new PacketBuffer(event.getPacket().payload());
		int iPacketID = payLoad.copy().readInt();
		
		if (iPacketID == PacketIDs.Settings_Veinator.value()) {
			SettingsVeinator.readPacketData(payLoad);

			myGlobals.lToolIDs = Globals.IDListToArray(SettingsVeinator.lToolIDs, false);
			myGlobals.lBlockIDs = Globals.IDListToArray(SettingsVeinator.lOreIDs, true);
		}
		else if (SettingsVeinator.bEnabled && iPacketID == PacketIDs.BLOCKINFO.value()) {
			SMPacket packet = new SMPacket();
			packet.readPacketData(payLoad);
			executeVeinator(packet, ((NetHandlerPlayServer)event.getHandler()).playerEntity);
		}
	}

	protected static void executeVeinator(SMPacket p, EntityPlayerMP player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (!isAllowedToMine(player, p)) return;

		mineVein(world, player, p);
		
		myGlobals.clearHistory();
	}

	@SuppressWarnings("deprecation")
	private static boolean isAllowedToMine(EntityPlayer player, SMPacket p) {
        IBlockState state = player.worldObj.getBlockState(p.oPos);
		Block block = state.getBlock();
		if (null == block || Blocks.AIR == block || Blocks.BEDROCK == block) return false;

        state = block.getActualState(state, player.worldObj, p.oPos);
		ItemStack oEquippedItem = player.getHeldItemMainhand();
		if (state.getMaterial().isToolNotRequired() ||
			oEquippedItem == null || 
			oEquippedItem.stackSize <= 0 ||
			!Globals.isIdInList(oEquippedItem.getItem(), myGlobals.lToolIDs) ||
			!oEquippedItem.canHarvestBlock(state) ||
			!ForgeHooks.canToolHarvestBlock(player.worldObj, p.oPos, oEquippedItem)) return false;

		if (p.flag_rs) return Globals.isIdInList(Blocks.REDSTONE_ORE, myGlobals.lBlockIDs) || Globals.isIdInList(Blocks.LIT_REDSTONE_ORE, myGlobals.lBlockIDs);

		return Globals.isIdInList(block, myGlobals.lBlockIDs);
	}

	private static void mineVein(World world, EntityPlayerMP player, SMPacket packet) {
		bMiningVein = true;
		
		myGlobals.checkConnection(world, packet.oPos, packet, true);
		
		while (breakBlock(world, player, packet)) { }
		
		packet.positions.clear();
		
		Globals.stackItems(world, player, new AxisAlignedBB(packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ(), packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ()).expand(16, 16, 16));
		
		if (SettingsVeinator.bGatherDrops) {
			List<Entity> list = Globals.getNearbyEntities(world, player.getEntityBoundingBox().expand(16, 16, 16));
			if (list != null && !list.isEmpty())
				for (Entity entity : list)
					if (!entity.isDead)
						entity.setPosition(packet.oPos.getX(), packet.oPos.getY(), packet.oPos.getZ());
		}
		
		bMiningVein = false;
	}

	private static boolean breakBlock(World world, EntityPlayerMP player, SMPacket p) {
		BlockPos blockPos = p.positions.poll();
		if (blockPos == null) return false;
		
		IBlockState state = world.getBlockState(blockPos);

		if (Globals.checkBlock(state, p)) {
			p.count += 1;
			
			while (SuperMiner_Core.isMCTicking()) try { Thread.sleep(1); } catch (InterruptedException e) { }
						
			// Double check that the block isn't air
			if (!world.isAirBlock(blockPos))
				player.interactionManager.tryHarvestBlock(blockPos);
			
			myGlobals.checkConnection(world, blockPos, p, true);
		}
		
		return true;
	}
	
	public void processIMC(final FMLInterModComms.IMCMessage imcMessage) {
        if (imcMessage.key.equalsIgnoreCase("MineVein"))
            if (imcMessage.isNBTMessage()) {
            	NBTTagCompound nbt = imcMessage.getNBTValue();

            	SMPacket iPacket = new SMPacket();
				iPacket.readPacketData(new PacketBuffer(Unpooled.copiedBuffer(nbt.getByteArray("MineVein"))));

				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				if (null == server) return;
				EntityPlayerMP player = (EntityPlayerMP)server.getEntityWorld().getEntityByID(iPacket.playerID);
				if (player == null) return;
				
				executeVeinator(iPacket, player);
            }
	}
}