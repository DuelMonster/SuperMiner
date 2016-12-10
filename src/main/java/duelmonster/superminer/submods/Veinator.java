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
import duelmonster.superminer.config.SettingsVeinator;
import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.network.packets.PacketIDs;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.objects.ExcavationHelper;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.util.BlockPos;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

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
			            	String sID = Item.itemRegistry.getNameForObject(item.getItem()).toString().trim();
			            	boolean bMCIncluded = sID.startsWith("minecraft:"); 
			                if ((bMCIncluded && !SettingsVeinator.lOreIDs.contains(sID.substring(10))) || (!bMCIncluded && !SettingsVeinator.lOreIDs.contains(sID))) {
			                	SettingsVeinator.lOreIDs.add(sID);
			                	
			            		SuperMiner_Core.configFile.get(MODID, Globals.localize("superminer.veinator.ore_ids"), SettingsVeinator.lOreIDDefaults.toArray(new String[0]), Globals.localize("superminer.veinator.ore_ids.desc")).set(SettingsVeinator.lOreIDs.toArray(new String[0]));
			            		SuperMiner_Core.configFile.save();
			            		
			        			try {
			        				int id = Integer.parseInt(sID.trim());
			        				myGlobals.lBlockIDs.add(Block.blockRegistry.getObjectById(id));
			        			} catch (NumberFormatException e) {
			        				ResourceLocation rlID = new ResourceLocation(sID);
			        				myGlobals.lBlockIDs.add(Block.blockRegistry.getObject(rlID.toString()));
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
        				myGlobals.lToolIDs.add(Item.itemRegistry.getObjectById(id));
        			} catch (NumberFormatException e) {
						Object item = Item.itemRegistry.getObject(message.getStringValue());
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
			Globals.sendPacket(new C17PacketCustomPayload(ChannelName, SettingsVeinator.writePacketData()));
			bShouldSyncSettings = false;
		}
		
		EntityPlayer player = mc.thePlayer;
		if (null == player || player.isDead || player.isPlayerSleeping()) return;

		World world = mc.theWorld;
		if (world != null) {
			
			Block block = null;
            int x = -1;
            int y = -1;
            int z = -1;
					
			if (player.getHealth() > 0.0F
					&& mc.objectMouseOver != null 
					&& mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
	            x = mc.objectMouseOver.blockX;
	            y = mc.objectMouseOver.blockY;
	            z = mc.objectMouseOver.blockZ;
	            
	    		block = world.getBlock(x, y, z);
	    		
	            if(!Globals.isAttacking(mc) && block != null && block == Blocks.air)
	            	block = null;
			}
			
			if (block != null && Globals.isIdInList(block, myGlobals.lBlockIDs)) {
				if (!bHungerNotified && player.getFoodStats().getFoodLevel() <= Globals.MIN_HUNGER) {
					Globals.NotifyClient(Globals.tooHungry() + MODName);
					bHungerNotified = true;
					return;
				}
				
				if (player.getFoodStats().getFoodLevel() > Globals.MIN_HUNGER) {
		    		int metadata = world.getBlockMetadata(x, y, z);
		    		myGlobals.addAttackBlock(
		    				player,
							block, metadata,
							new BlockPos(x, y, z),
							false, true, true, false);
				}
			} else bHungerNotified = false;
			
			// Removes packets from the history.
			for (Iterator<SMPacket> attackPackets = myGlobals.attackHistory.iterator(); attackPackets.hasNext();) {
				SMPacket packet = (SMPacket)attackPackets.next();
				if (System.nanoTime() - packet.nanoTime >= Globals.attackHistoryDelayNanoTime)
					attackPackets.remove(); // Removes packet from the history if it has been there too long.
				else {
					block = world.getBlock(packet.oPos.getX(), packet.oPos.getZ(), packet.oPos.getZ());
					if (block == null || block == Blocks.air) {
						attackPackets.remove(); // Removes packet from the history.
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
		
		if (iPacketID == PacketIDs.Settings_Veinator.value()) {
			SettingsVeinator.readPacketData(payLoad);

			myGlobals.lToolIDs = Globals.IDListToArray(SettingsVeinator.lToolIDs, false);
			myGlobals.lBlockIDs = Globals.IDListToArray(SettingsVeinator.lOreIDs, true);
		}
		else if (SettingsVeinator.bEnabled && iPacketID == PacketIDs.BLOCKINFO.value()) {
			SMPacket packet = new SMPacket();
			packet.readPacketData(payLoad);
			executeVeinator(packet, ((NetHandlerPlayServer)event.handler).playerEntity);
		}
	}

	protected static void executeVeinator(SMPacket packet, EntityPlayerMP player) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) return;
		
		World world = server.worldServerForDimension(player.dimension);
		if (!isAllowedToMine(player, packet)) return;

		ExcavationHelper oEH = new ExcavationHelper(world, player, packet);
		myExcavationHelpers.add(oEH);
		oEH.getOreVein();
		if (!oEH.ExcavateSection()) {
			oEH.FinalizeVeination();
			myExcavationHelpers.remove(oEH);
		}
		
		myGlobals.clearHistory();
	}

	@SubscribeEvent
	public void tickEvent_Server(TickEvent.ServerTickEvent event) {
		if (!SettingsVeinator.bEnabled || !TickEvent.Phase.END.equals(event.phase)) return;
		
		// Retrieve any FMLInterModComm messages that may have been sent from other mods
		for (final FMLInterModComms.IMCMessage imcMessage : FMLInterModComms.fetchRuntimeMessages(this.instance))
			processIMC(imcMessage);

		if (myExcavationHelpers.size() > 0)
	    	for (ExcavationHelper oEH : getMyExcavationHelpers()) 
				if (oEH.isExcavating() && !oEH.ExcavateSection()) {
					oEH.FinalizeVeination();
					myExcavationHelpers.remove(oEH);
				}
	}

	private static boolean isAllowedToMine(EntityPlayer player, SMPacket p) {
		Block block = p.block;
		if (null == block || Blocks.air == block || Blocks.bedrock == block) return false;

		ItemStack oEquippedItem = player.getCurrentEquippedItem();
		if (block.getMaterial().isToolNotRequired() ||
			oEquippedItem == null || 
			oEquippedItem.stackSize <= 0 ||
			!Globals.isIdInList(oEquippedItem.getItem(), myGlobals.lToolIDs) ||
			!ForgeHooks.canHarvestBlock(block, player, p.metadata) ||
			!ForgeHooks.canToolHarvestBlock(block, p.metadata, oEquippedItem)) return false;

		if (p.flag_rs) return Globals.isIdInList(Blocks.redstone_ore, myGlobals.lBlockIDs) || Globals.isIdInList(Blocks.lit_redstone_ore, myGlobals.lBlockIDs);

		return Globals.isIdInList(block, myGlobals.lBlockIDs);
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