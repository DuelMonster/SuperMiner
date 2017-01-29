package duelmonster.superminer.objects;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.gson.Gson;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Veinator;
import duelmonster.superminer.util.BlockPos;
import duelmonster.superminer.util.EnumFacing;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class Globals {
	private static Gson gson = new Gson();
	
	public static final String ChannelName = "SuperMiner";
	
	public static String tooHungry() {
		return "§c" + Globals.localize("superminer.hungry");
	}
	
	public static final int		FLAG_CHANGE_NUM				= 10;
	public static final long	attackHistoryDelayNanoTime	= 3000000000L;
	public static final long	packetWaitMilliSec			= 200L;
	public static final int		MIN_HUNGER					= 1;
	
	public static final int	SIDE_NONE	= -1;
	public static final int	SIDE_Bottom	= 0;
	public static final int	SIDE_Top	= 1;
	public static final int	SIDE_North	= 2;
	public static final int	SIDE_South	= 3;
	public static final int	SIDE_West	= 4;
	public static final int	SIDE_East	= 5;
	
	public long packetEnableTime = System.currentTimeMillis() + packetWaitMilliSec;
	
	public List<Object>	lToolIDs	= new ArrayList<Object>();
	public List<Object>	lBlockIDs	= new ArrayList<Object>();
	public List<Object>	lLeafIDs	= new ArrayList<Object>();
	
	public int		iBlockRadius	= -1;
	public int		iBlockLimit		= 64 * 4;
	public int		iBlocksFound	= 0;
	public BlockPos	initalBlockPos;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BlockingQueue<SMPacket> attackHistory = new LinkedBlockingQueue();
	
	public static String getLatestVerion(String sURL) {
		try {
			return Globals.gson.fromJson(IOUtils.toString(new URL(sURL)), VersionContainer.class).getModVersion();
		} catch (Exception e) {}
		return "";
	}
	
	public void clearHistory() {
		iBlocksFound = 0;
		initalBlockPos = null;
		attackHistory.clear();
	}
	
	public void addAttackBlock(EntityPlayer player, Block block, int metadata, BlockPos oPos, boolean bClearHistory, boolean bCheckBlockIDs, boolean bCheckToolIDs, boolean bLayerOnly) {
		addAttackBlock(player, block, metadata, oPos, EnumFacing.UP, bClearHistory, bCheckBlockIDs, bCheckToolIDs, bLayerOnly);
	}
	
	public void addAttackBlock(EntityPlayer player, Block block, int metadata, BlockPos oPos, EnumFacing sideHit, boolean bClearHistory, boolean bCheckBlockIDs, boolean bCheckToolIDs, boolean bLayerOnly) {
		
		if (player == null) return;
		
		if (bClearHistory) clearHistory();
		
		Item equipedItemId = (null == player.getCurrentEquippedItem() ? null : player.getCurrentEquippedItem().getItem());
		
		boolean bCanAttack = ((!bCheckBlockIDs || !bCheckToolIDs || (bCheckBlockIDs && isIdInList(block, lBlockIDs)) && (bCheckToolIDs && isIdInList(equipedItemId, lToolIDs))));
		
		for (Iterator<SMPacket> oCurrentPacket = attackHistory.iterator(); oCurrentPacket.hasNext();) {
			SMPacket oPacket = oCurrentPacket.next();
			if (bCanAttack && oPos.equals(oPacket.oPos)) {
				oPacket.nanoTime = System.nanoTime();
				oPacket.prevBlock = oPacket.block;
				oPacket.prevMetadata = oPacket.metadata;
				oPacket.block = block;
				oPacket.metadata = metadata;
				oPacket.bLayerOnlyToggled = bLayerOnly;
				bCanAttack = false;
			}
		}
		
		if (bCanAttack) {
			SMPacket oPacket = new SMPacket();
			oPacket.oPos = oPos;
			oPacket.sideHit = sideHit;
			oPacket.block = block;
			oPacket.metadata = metadata;
			oPacket.nanoTime = System.nanoTime();
			oPacket.flag_rs = (block == Blocks.redstone_ore || block == Blocks.lit_redstone_ore);
			oPacket.bLayerOnlyToggled = bLayerOnly;
			
			try {
				attackHistory.put(oPacket);
			} catch (InterruptedException e) {}
		}
	}
	
	public static boolean isIdInList(Object oID, List<Object> list) {
		return (list != null && list.indexOf(oID) >= 0);
	}
	
	public static List<Object> IDListToArray(String[] saIDs, boolean bIsBlock) {
		return IDListToArray(Arrays.asList(saIDs), bIsBlock);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Object> IDListToArray(List<String> saIDs, boolean bIsBlock) {
		RegistryNamespaced rn = bIsBlock ? Block.blockRegistry : Item.itemRegistry;
		List<Object> lReturn = new ArrayList();
		
		for (String sID : saIDs) {
			Object oEntity = null;
			sID = sID.trim();
			
			try {
				int id = Integer.parseInt(sID.trim());
				oEntity = rn.getObjectById(id);
			} catch (NumberFormatException e) {
				oEntity = rn.getObject(sID);
			}
			
			if (null != oEntity && Blocks.air != oEntity) lReturn.add(oEntity);
			
		}
		return lReturn;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Entity> getNearbyEntities(World world, AxisAlignedBB area) {
		if (!Excavator.isExcavating() &&
				!Shaftanator.isExcavating() &&
				!Veinator.isExcavating()) {
			
			try {
				List<?> list = world.getEntitiesWithinAABB(Entity.class, area);
				if (null == list || list.isEmpty()) return null;
				
				List<Entity> ret = new ArrayList();
				for (Object o : list) {
					Entity e = (Entity) o;
					if (!e.isDead && (e instanceof EntityItem || e instanceof EntityXPOrb))
						ret.add(e);
				}
				return ret.isEmpty() ? null : ret;
				
			} catch (ConcurrentModificationException e) {
				return null;
			}
		}
		return null;
	}
	
	public void checkConnection(World world, BlockPos oPos, SMPacket p, boolean bDestroyUnder) {
		checkConnection(world, oPos, p, bDestroyUnder, true);
	}
	
	public void checkConnection(World world, BlockPos oPos, SMPacket p, boolean bDestroyUnder, boolean bCheckMetadata) {
		int iPosStartEnd = 1;
		int yStart = ((!bDestroyUnder && p.oPos.getY() == oPos.getY()) ? 0 : 1);
		
		if (null == initalBlockPos) initalBlockPos = new BlockPos(oPos);
		
		for (int xOffset = -iPosStartEnd; xOffset <= iPosStartEnd; xOffset++)
			for (int yOffset = -yStart; yOffset <= iPosStartEnd; yOffset++)
				for (int zOffset = -iPosStartEnd; zOffset <= iPosStartEnd; zOffset++) {
					if (xOffset == 0 && yOffset == 0 && zOffset == 0)
						continue;
					
					BlockPos blockPos = oPos.add(xOffset, yOffset, zOffset);
					
					if (iBlockRadius > 0 && !isWithinRange(initalBlockPos, blockPos, iBlockRadius)) continue;
					
					int iMetadata = (bCheckMetadata ? world.getBlockMetadata(blockPos.getX(), blockPos.getY(), blockPos.getZ()) : -1);
					if (checkBlock(world.getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ()), iMetadata, p)) {
						p.positions.offer(blockPos);
						
						iBlocksFound++;
						if (iBlocksFound >= iBlockLimit) return;
					}
				}
	}
	
	public static boolean checkBlock(Block block, SMPacket p) {
		return checkBlock(block, -1, p);
	}
	
	public static boolean checkBlock(Block block, int iMetadata, SMPacket p) {
		if (block == null || Blocks.air == block || Blocks.bedrock == block) return false;
		if (p != null) {
			// if (block == p.block && iMetadata == p.metadata) return false;
			if (block == p.block && (block instanceof BlockRotatedPillar || (iMetadata > -1 && block instanceof BlockQuartz && (iMetadata == 2 || iMetadata == 3 || iMetadata == 4)))) return true;
			if (p.flag_rs && (block == Blocks.redstone_ore || block == Blocks.lit_redstone_ore)) return true;
			if (block != p.block) return false; // || (iMetadata > -1 && p.metadata != iMetadata)) return false;
			return true;
		}
		return false;
	}
	
	public static void NotifyClient(String sMsg) {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§5[SuperMiner] §f" + sMsg));
	}
	
	public static void NotifyClient(boolean bIsOn, String sModName) {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§5[SuperMiner] §6" + sModName + " " + (bIsOn ? "§aON" : "§cOFF")));
	}
	
	public static boolean isAttacking(Minecraft mc) {
		return Globals.isKeyDownEx(mc.gameSettings.keyBindAttack.getKeyCode());
	}
	
	public static boolean isUsingItem(Minecraft mc) {
		return Globals.isKeyDownEx(mc.gameSettings.keyBindUseItem.getKeyCode());
	}
	
	public static int getKeyIndexEx(String name) {
		int index = Mouse.getButtonIndex(name);
		if (index == -1) {
			return Keyboard.getKeyIndex(name);
		} else {
			return index - 100;
		}
	}
	
	public static boolean isKeyDownEx(String name) {
		return isKeyDownEx(getKeyIndexEx(name));
	}
	
	public static boolean isKeyDownEx(int index) {
		if (index < 0) {
			return Mouse.isButtonDown(index + 100);
		} else {
			return Keyboard.isKeyDown(index);
		}
	}
	
	public static void playSound(World world, String sound, BlockPos oPos) {
		world.playSoundEffect(oPos.getX() + 0.5F, oPos.getY() + 0.5F, oPos.getZ() + 0.5F, sound, 2.0F, 1.0F);
	}
	
	public static void playSound(World world, SoundType sound, BlockPos oPos) {
		world.playSoundEffect(oPos.getX() + 0.5F, oPos.getY() + 0.5F, oPos.getZ() + 0.5F, sound.getStepResourcePath(), (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
	}
	
	public static String localize(String key) {
		if (StatCollector.canTranslate(key)) return StatCollector.translateToLocal(key);
		else return StatCollector.translateToFallback(key);
	}
	
	public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
		int distanceX = sourcePos.getX() - targetPos.getX();
		int distanceY = sourcePos.getY() - targetPos.getY();
		int distanceZ = sourcePos.getZ() - targetPos.getZ();
		
		return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
	}
	
	public static void sendPacket(Packet packetIn) {
		if (FMLCommonHandler.instance().getSide().isClient())
			FMLClientHandler.instance().getClient().getNetHandler().addToSendQueue(packetIn);
	}
	
	public static void sendPacket(Packet packetIn, EntityPlayerMP player) {
		if (FMLCommonHandler.instance().getSide().isServer())
			player.playerNetServerHandler.sendPacket(packetIn);
	}
}
