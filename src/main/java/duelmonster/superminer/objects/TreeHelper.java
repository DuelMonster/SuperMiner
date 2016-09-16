package duelmonster.superminer.objects;

import duelmonster.superminer.config.SettingsLumbinator;
import duelmonster.superminer.network.packets.SMPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

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
					Block nextBlock = oPlayer.worldObj.getBlockState(nextPos).getBlock();
					
					if (nextBlock.getMaterial() == Material.leaves && Globals.isIdInList(nextBlock, myGlobals.lLeafIDs)) {
						oFirstLeafPos = nextPos;
						return getUniqueIdentifier(nextPos);
					}
				}
		
		return "unknown";
	}
	
	public String getUniqueIdentifier(BlockPos oPos) {
		IBlockState state = oPlayer.worldObj.getBlockState(oPos);
		return getUniqueIdentifier(state.getBlock(), state.getBlock().getMetaFromState(state));
	}
	public String getUniqueIdentifier(Block block, int iMetadata) {
		String sResult = "";

		sResult = block.getUnlocalizedName() + "_"; // + iMetadata + "_";

		boolean bIsValidWood = (block.getMaterial() == Material.wood && Globals.isIdInList(block, myGlobals.lBlockIDs));
		boolean bIsValidLeaves = (block.getMaterial() == Material.leaves && Globals.isIdInList(block, myGlobals.lLeafIDs));
		
		if (bIsValidWood) {
			if (block instanceof BlockOldLog) {
				sResult += BlockPlanks.EnumType.byMetadata((iMetadata & 3) % 4).getName();
			} else if (block instanceof BlockNewLog) {
				sResult += BlockPlanks.EnumType.byMetadata((iMetadata & 3) + 4).getName();
			}
		} else if (bIsValidLeaves) {
			if (block instanceof BlockOldLeaf) {
				sResult += ((BlockOldLeaf)block).getWoodType(iMetadata).getName();
			} else if (block instanceof BlockNewLeaf) {
				sResult += ((BlockNewLeaf)block).getWoodType(iMetadata).getName();
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
		Block nextBlock = oPlayer.worldObj.getBlockState(nextPos).getBlock();

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