package duelmonster.superminer.objects;

import duelmonster.superminer.config.SettingsLumbinator;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

public class TreeHelper {
	public static final int MAX_TREE_WIDTH = 64;

	private EntityPlayer oPlayer;
	private Globals myGlobals;
	
	public int iTreeWidthPlusX = 0, iTreeWidthMinusX = 0;
	public int iTreeWidthPlusZ = 0, iTreeWidthMinusZ = 0;
	
	private BlockPos oFirstLeafPos = null;

	public TreeHelper(EntityPlayer player, Globals globals) {
		oPlayer = player;
		myGlobals = globals;
	}
	
	public void DetectTreeWidth(SMPacket currentPacket) {
		if (oFirstLeafPos != null) {
			int yLevel = oFirstLeafPos.getY();

			for (int yLeaf = yLevel; yLeaf < oPlayer.worldObj.getHeight(); yLeaf++) {
				String sUID = "";
				boolean bIsValidWood = false, bIsValidLeaf = false; 
				int iWidthPlusX = 0, iWidthMinusX = 0;
				int iWidthPlusZ = 0, iWidthMinusZ = 0;
				int iLastWoodPlus = 0, iLastWoodMinus = 0;
				
				for (int xOffset = 0; xOffset < MAX_TREE_WIDTH; xOffset++) {
					//Check X plus width
					sUID = getUniqueIdentifier(new BlockPos(currentPacket.oPos.getX() + xOffset, yLevel, currentPacket.oPos.getZ()));
					bIsValidWood = currentPacket.sWoodName.equalsIgnoreCase(sUID);
					bIsValidLeaf = currentPacket.sLeafName.equalsIgnoreCase(sUID);
					
					if (bIsValidWood) iLastWoodPlus = xOffset;
					if (bIsValidLeaf && (xOffset - iLastWoodPlus) > SettingsLumbinator.iLeafRange) break;
					if (bIsValidWood || bIsValidLeaf) iWidthPlusX++;
					
					//Check X minus width
					sUID = getUniqueIdentifier(new BlockPos(currentPacket.oPos.getX() - xOffset, yLevel, currentPacket.oPos.getZ()));
					bIsValidWood = currentPacket.sWoodName.equalsIgnoreCase(sUID);
					bIsValidLeaf = currentPacket.sLeafName.equalsIgnoreCase(sUID);
					
					if (bIsValidWood) iLastWoodMinus = xOffset;
					if (bIsValidLeaf && (xOffset - iLastWoodMinus) > SettingsLumbinator.iLeafRange) break;
					if (bIsValidWood || bIsValidLeaf) iWidthMinusX++;
				}
				
				iLastWoodPlus = 0; iLastWoodMinus = 0;
				for (int zOffset = 0; zOffset < MAX_TREE_WIDTH; zOffset++) {
					//Check Z plus width
					sUID = getUniqueIdentifier(new BlockPos(currentPacket.oPos.getX(), yLevel, currentPacket.oPos.getZ() + zOffset));
					bIsValidWood = currentPacket.sWoodName.equalsIgnoreCase(sUID);
					bIsValidLeaf = currentPacket.sLeafName.equalsIgnoreCase(sUID);
					
					if (bIsValidWood) iLastWoodPlus = zOffset;
					if (bIsValidLeaf && (zOffset - iLastWoodPlus) > SettingsLumbinator.iLeafRange) break;
					if (bIsValidWood || bIsValidLeaf) iWidthPlusZ++;
					
					//Check Z minus width
					sUID = getUniqueIdentifier(new BlockPos(currentPacket.oPos.getX(), yLevel, currentPacket.oPos.getZ() - zOffset));
					bIsValidWood = currentPacket.sWoodName.equalsIgnoreCase(sUID);
					bIsValidLeaf = currentPacket.sLeafName.equalsIgnoreCase(sUID);
					
					if (bIsValidWood) iLastWoodMinus = zOffset;
					if (bIsValidLeaf && (zOffset - iLastWoodMinus) > SettingsLumbinator.iLeafRange) break;
					if (bIsValidWood || bIsValidLeaf) iWidthMinusZ++;
				}
				
				if (iWidthPlusX > iTreeWidthPlusX) iTreeWidthPlusX = iWidthPlusX;
				if (iWidthMinusX > iTreeWidthMinusX) iTreeWidthMinusX = iWidthMinusX;
				if (iWidthPlusZ > iTreeWidthPlusZ) iTreeWidthPlusZ = iWidthPlusZ;
				if (iWidthMinusZ > iTreeWidthMinusZ) iTreeWidthMinusZ = iWidthMinusZ;
				
			}
		}
	}
	
	public String getLeafName(SMPacket currentPacket) {
		for (int yLeaf = currentPacket.oPos.getY() - 1; yLeaf < oPlayer.worldObj.getHeight(); yLeaf++)
			for (int xLeaf = -1; xLeaf < 1; xLeaf++)
				for (int zLeaf = -1; zLeaf < 1; zLeaf++) {
					BlockPos nextPos = new BlockPos(currentPacket.oPos.getX() + xLeaf, yLeaf, currentPacket.oPos.getZ() + zLeaf);
					Block nextBlock = oPlayer.worldObj.getBlock(nextPos.getX(), nextPos.getY(), nextPos.getZ());
		
					if (nextBlock.getMaterial() == Material.leaves && Globals.isIdInList(nextBlock, myGlobals.lLeafIDs)) {
						oFirstLeafPos = nextPos;
						return getUniqueIdentifier(nextPos);
					}
				}
		
		return "unknown";
	}
	
	public String getUniqueIdentifier(BlockPos oPos) {
		return getUniqueIdentifier(oPlayer.worldObj.getBlock(oPos.getX(), oPos.getY(), oPos.getZ()), oPlayer.worldObj.getBlockMetadata(oPos.getX(), oPos.getY(), oPos.getZ()));
	}
	public String getUniqueIdentifier(Block block, int iMetadata) {
		String sResult = "";

		sResult = block.getUnlocalizedName() + "_"; // + iMetadata + "_";

		boolean bIsValidWood = (block.getMaterial() == Material.wood && Globals.isIdInList(block, myGlobals.lBlockIDs));
		boolean bIsValidLeaves = (block.getMaterial() == Material.leaves && Globals.isIdInList(block, myGlobals.lLeafIDs));
		
		if (bIsValidWood) {
			int iWoodType = 0;
			
			if (block instanceof BlockOldLog) {
				
				iWoodType = ((iMetadata >> 0) & 0x1) & ((iMetadata >> 0) & 0x2);
				
				sResult += BlockOldLog.field_150168_M[iWoodType];
			} else if (block instanceof BlockNewLog) {
				iWoodType = ((iMetadata >> 0) & 0x1);
				
				sResult += BlockNewLog.field_150169_M[iWoodType];
			}
		} else if (bIsValidLeaves) {
			int iLeafType = 0;

			if (block instanceof BlockOldLeaf) {
				iLeafType = ((iMetadata >> 0) & 0x1) & ((iMetadata >> 0) & 0x2);
				
				sResult += BlockOldLeaf.field_150130_N[0][iLeafType];
			} else if (block instanceof BlockNewLeaf) {
				iLeafType = ((iMetadata >> 0) & 0x1);
						
				sResult += BlockNewLeaf.field_150132_N[0][iLeafType];
			}
		}
		
		return sResult;
	}
	
	public boolean ensureIsConnectedToTree(SMPacket currentPacket, BlockPos nextPos) {
		// Ensure we have a direct connection to the current Tree.
		for (int yConOffset = -1; yConOffset <= 1; yConOffset++)
			for (int xConOffset = -1; xConOffset <= 1; xConOffset++)
				for (int zConOffset = -1; zConOffset <= 1; zConOffset++) {
					BlockPos oConPos = new BlockPos(nextPos.getX() + xConOffset, nextPos.getY() + yConOffset, nextPos.getZ() + zConOffset);
					
					if (currentPacket.positions.contains(oConPos))
						return true;
			}

		return false;
	}
	
	public boolean processPosition(SMPacket currentPacket, BlockPos nextPos, boolean bIsTree) {
		Block nextBlock = oPlayer.worldObj.getBlock(nextPos.getX(), nextPos.getY(), nextPos.getZ());

		if (nextBlock != null && nextBlock != Blocks.air && ensureIsConnectedToTree(currentPacket, nextPos)) {
			boolean bIsValidWood = (nextBlock.getMaterial() == Material.wood && Globals.isIdInList(nextBlock, myGlobals.lBlockIDs));
			boolean bIsValidLeaves = (nextBlock.getMaterial() == Material.leaves && Globals.isIdInList(nextBlock, myGlobals.lLeafIDs));
			String sNextName = getUniqueIdentifier(nextPos);
			
			if ((bIsValidWood && currentPacket.sWoodName.equalsIgnoreCase(sNextName)) || 
				(bIsValidLeaves && (currentPacket.sLeafName.isEmpty() || currentPacket.sLeafName.equalsIgnoreCase(sNextName)))) {
				
				if (bIsValidLeaves && currentPacket.sLeafName.isEmpty())
					currentPacket.sLeafName = sNextName;
				
				if (bIsValidWood || (bIsValidLeaves && SettingsLumbinator.bDestroyLeaves))
					currentPacket.positions.offer(nextPos);

				if (bIsValidLeaves && !bIsTree) return true;

			} else if (SettingsLumbinator.bDestroyLeaves && nextBlock instanceof BlockSnow)
				currentPacket.positions.offer(nextPos);
		}
		
		return bIsTree;
	}
}
