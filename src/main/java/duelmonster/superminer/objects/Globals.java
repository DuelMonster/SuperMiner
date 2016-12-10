package duelmonster.superminer.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Veinator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Globals {
    public static final String ChannelName = "SuperMiner";
    
	public static String tooHungry() {
		return "§c" + Globals.localize("superminer.hungry");
	}

	public static final int FLAG_CHANGE_NUM = 10;
	public static final long attackHistoryDelayNanoTime = 3000000000L;
	public static final long packetWaitMilliSec = 200L;
	public static final int MIN_HUNGER = 1;

	public static final int SIDE_NONE = -1;
	public static final int SIDE_Bottom = 0;
	public static final int SIDE_Top = 1;
	public static final int SIDE_North = 2;
	public static final int SIDE_South = 3;
	public static final int SIDE_West = 4;
	public static final int SIDE_East = 5;

	public long packetEnableTime = System.currentTimeMillis() + packetWaitMilliSec;

	public List<Object> lToolIDs = new ArrayList<Object>();
	public List<Object> lBlockIDs = new ArrayList<Object>();
	public List<Object> lLeafIDs = new ArrayList<Object>();

	public int iBlockRadius = -1;
	public int iBlockLimit = 64 * 4;
	public int iBlocksFound = 0;
	public BlockPos initalBlockPos;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BlockingQueue<SMPacket> attackHistory = new LinkedBlockingQueue();
	
	public void clearHistory(){
		iBlocksFound = 0;
		initalBlockPos = null;
		attackHistory.clear();
	}

	public void addAttackBlock(EntityPlayer player, IBlockState state, BlockPos oPos, boolean bClearHistory, boolean bCheckBlockIDs, boolean bCheckToolIDs, boolean bLayerOnly) {
		addAttackBlock(player, state, oPos, EnumFacing.UP, bClearHistory, bCheckBlockIDs, bCheckToolIDs, bLayerOnly);
	}
	public void addAttackBlock(EntityPlayer player, IBlockState state, BlockPos oPos, EnumFacing sideHit, boolean bClearHistory, boolean bCheckBlockIDs, boolean bCheckToolIDs, boolean bLayerOnly) {
		
		if (player == null) return;
		
		if (bClearHistory) clearHistory();
		
		Item equipedItemId = (null == player.getHeldItemMainhand() ? null : player.getHeldItemMainhand().getItem());
		
		Block block = state.getBlock();
		
		boolean bCanAttack = ((!bCheckBlockIDs || !bCheckToolIDs || (bCheckBlockIDs && isIdInList(block, lBlockIDs)) && (bCheckToolIDs && isIdInList(equipedItemId, lToolIDs))));
		
		for (Iterator<SMPacket> oCurrentPacket = attackHistory.iterator(); oCurrentPacket.hasNext();) {
			SMPacket oPacket = (SMPacket)oCurrentPacket.next();
			if (bCanAttack && oPos.equals(oPacket.oPos)) {
				oPacket.nanoTime = System.nanoTime();
				oPacket.prevBlock = oPacket.block;
				oPacket.block = block;
				oPacket.bLayerOnlyToggled = bLayerOnly;
				bCanAttack = false;
			}
		}
		
		if (bCanAttack) {
			SMPacket oPacket = new SMPacket();
			oPacket.oPos = oPos;
			oPacket.sideHit = sideHit;
			oPacket.block = block;
			oPacket.metadata = block.getMetaFromState(state);
			oPacket.nanoTime = System.nanoTime();
			oPacket.flag_rs = (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE);
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
		RegistryNamespaced rn = bIsBlock ? Block.REGISTRY : Item.REGISTRY;
		List<Object> lReturn = new ArrayList();
		
		for (String sID : saIDs) {
			Object oEntity = null;
			sID = sID.trim();
			
			try {
				int id = Integer.parseInt(sID.trim());
				oEntity = rn.getObjectById(id);
			} catch (NumberFormatException e) {
				oEntity = rn.getObject(new ResourceLocation(sID));
			}
			
			if (null != oEntity && Blocks.AIR != oEntity) lReturn.add(oEntity);

		}
		return lReturn;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Entity> getNearbyEntities(World world, AxisAlignedBB area) {
		if (!Excavator.isExcavating() && 
			!Shaftanator.isExcavating() &&
			!Veinator.isExcavating()) {
			
			try{
				List<?> list = world.getEntitiesWithinAABB(Entity.class, area);
				if (null == list || list.isEmpty()) return null;
	
				List<Entity> ret = new ArrayList();
				for (Object o : list) {
					Entity e = (Entity) o;
					if (!e.isDead && (e instanceof EntityItem || e instanceof EntityXPOrb))
						ret.add(e);
				}
				return ret.isEmpty() ? null : ret;
				
			} catch (ConcurrentModificationException e){
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

					if (checkBlock(world.getBlockState(blockPos), p)) {
						p.positions.offer(blockPos);

						iBlocksFound++;
						if (iBlocksFound >= iBlockLimit) return;
					}
				}
	}

	public static boolean checkBlock(IBlockState state, SMPacket p) {
		return checkBlock(state, p, false); 
	}
	public static boolean checkBlock(IBlockState state, SMPacket p, boolean bDetectVariants) {
		Block block = state.getBlock();
		int iMetadata = block.getMetaFromState(state);
		
		if (block == null || Blocks.AIR == block || Blocks.BEDROCK == block) return false;
		if (p != null) {
			if (bDetectVariants && block instanceof BlockStone && iMetadata != p.metadata) return false;
			if (block == p.block && (block instanceof BlockRotatedPillar || (iMetadata > -1 && block instanceof BlockQuartz && (iMetadata == 2 || iMetadata == 3 || iMetadata == 4)))) return true;
			if (p.flag_rs && (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE)) return true;
			if (block != p.block) return false; // || (iMetadata > -1 && p.metadata != iMetadata)) return false;
			return true;
		}
		return false;
	}

	public static void stackItems(World world, EntityPlayer entityplayer, SMPacket p) {
		stackItems(world, entityplayer, new AxisAlignedBB(p.oPos.getX(), p.oPos.getY(), p.oPos.getZ(), p.oPos.getX() + 1.0D, p.oPos.getY() + 1.0D, p.oPos.getZ() + 1.0D));
	}
	public static void stackItems(World world, EntityPlayer entityplayer, AxisAlignedBB box) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer, box);
		if (list == null || list.isEmpty()) return;

		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity)list.get(i);
			if (entity1 instanceof EntityItem && !entity1.isDead) {
				EntityItem e1 = (EntityItem)entity1;
				ItemStack e1Item = e1.getEntityItem();
				int itemDurability1 = e1Item.getMaxDamage();
				for (int j = i + 1; j < list.size(); j++) {
					Entity entity2 = (Entity)list.get(j);
					if (entity2 instanceof EntityItem && !entity2.isDead) {
						EntityItem e2 = (EntityItem)entity2;
						ItemStack e2Item = e2.getEntityItem();
						int itemDurability2 = e2Item.getMaxDamage();
						if (e1Item.getItem() == e2Item.getItem() && itemDurability1 == itemDurability2) {
							e1Item.stackSize += e2Item.stackSize;
							entity2.setDead();
						}
					}
				}
				e1.setEntityItemStack(e1Item);
			}
		}
	}

	public static void NotifyClient(String sMsg){
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§5[SuperMiner] §f" + sMsg));
	}
	public static void NotifyClient(boolean bIsOn, String sModName){
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§5[SuperMiner] §6" + sModName + " " + (bIsOn ? "§aON" : "§cOFF")));
	}
	
	public static boolean isAttacking(Minecraft mc) {
		return mc.gameSettings.keyBindAttack.isKeyDown();
	}
	
	public static boolean isUsingItem(Minecraft mc) {
		return mc.gameSettings.keyBindUseItem.isKeyDown();
	}
	
	public static int getKeyIndexEx(String name){
		int index = Mouse.getButtonIndex(name);
		if (index == -1){
			return Keyboard.getKeyIndex(name);
		} else {
			return index - 100;
		}
	}
	
	public static boolean isKeyDownEx(String name){
		return isKeyDownEx(getKeyIndexEx(name));
	}
	public static boolean isKeyDownEx(int index){
		if (index < 0){
			return Mouse.isButtonDown(index + 100);
		} else {
			return Keyboard.isKeyDown(index);
		}
	}
	
	public static void playSound(World world, SoundEvent sound, BlockPos oPos) {
		world.playSound((EntityPlayer)null, (double)((float)oPos.getX() + 0.5F), (double)((float)oPos.getY() + 0.5F), (double)((float)oPos.getZ() + 0.5F), sound, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}

	public static String localize(String key) {
		if (I18n.hasKey(key))
			return I18n.format(key);
		else
			return key;
	}
	
    public static boolean isWithinRange(BlockPos sourcePos, BlockPos targetPos, int range) {
        int distanceX = sourcePos.getX() - targetPos.getX();
        int distanceY = sourcePos.getY() - targetPos.getY();
        int distanceZ = sourcePos.getZ() - targetPos.getZ();
        
        return ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ)) <= (range * range);
    }

	public static void sendPacket(Packet<?> packetIn) {
		if (FMLCommonHandler.instance().getSide().isClient())
			FMLClientHandler.instance().getClientToServerNetworkManager().sendPacket(packetIn);
	}
	public static void sendPacket(Packet<?> packetIn, EntityPlayerMP player) {
		if (FMLCommonHandler.instance().getSide().isServer())
			player.connection.sendPacket(packetIn);
	}
}
