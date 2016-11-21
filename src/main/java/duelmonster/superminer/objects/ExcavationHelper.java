package duelmonster.superminer.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.SettingsExcavator;
import duelmonster.superminer.config.SettingsShaftanator;
import duelmonster.superminer.network.packets.IlluminatorPacket;
import duelmonster.superminer.network.packets.SMPacket;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Veinator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ExcavationHelper {
	private final int SECTION_LIMIT = 4;

	World world;
	EntityPlayerMP player;
	SMPacket oPacket;
	BlockPos oInitialPos = null;
	boolean bLayerOnlyToggled = false;
	EnumFacing sideHit;
	int iBlocksFound = 0;
	int iUnconnectedCount = 0;
	LinkedList<BlockPos> oPositions = new LinkedList<BlockPos>();
	int iSpiral = 1;
	boolean bIsExcavating = true;
	int iLowestY = 0;
	int iFeetPos = 0;
	int iLengthStart = 0;
	int iLengthEnd = 0;
	int iWidthStart = 0;
	int iWidthEnd = 0;

	private boolean bAutoIlluminate() { return SettingsExcavator.bAutoIlluminate || SettingsShaftanator.bAutoIlluminate; }
	private boolean bMineVeins() { return /*SettingsExcavator.bMineVeins ||*/ SettingsShaftanator.bMineVeins; }
	
	public boolean isExcavating() { return bIsExcavating; }
	
	public ExcavationHelper(World world, EntityPlayerMP player, SMPacket oPacket) {
		this.world = world;
		this.player = player;
		this.oPacket = oPacket;
		this.oInitialPos = new BlockPos(oPacket.oPos);
		this.bLayerOnlyToggled = oPacket.bLayerOnlyToggled;
		this.sideHit = oPacket.sideHit;
	}

	/**
	 * Excavate a Section of Blocks.
	 * @return false if no blocks left to process.
	 */
	public boolean ExcavateSection() {
		if (!oPositions.isEmpty()) {
			if (!bIsExcavating) bIsExcavating = true;
			
			for (int indx = 0; indx <= SECTION_LIMIT; indx++)
				if (!oPositions.isEmpty()) {
					BlockPos workingPos = null;
					try {
						workingPos = oPositions.removeFirst();
					} catch (NoSuchElementException e) {
						bIsExcavating = false;
						return false;
					}
					
					if (workingPos != null && !world.isAirBlock(workingPos)) {
						IBlockState state = world.getBlockState(workingPos);
						Block block = state.getBlock();
						
						// Mine Ore Veins if the Veinator is enabled
						if (this.bMineVeins() && Globals.isIdInList(block, Veinator.myGlobals.lBlockIDs)) {
							// Setup the Veinator data packet
							SMPacket oVeinPacket = new SMPacket();
							oVeinPacket.oPos = new BlockPos(workingPos);
							oVeinPacket.sideHit = sideHit;
							oVeinPacket.block = block;
							oVeinPacket.metadata = block.getMetaFromState(state);
							oVeinPacket.nanoTime = System.nanoTime();
							oVeinPacket.flag_rs = (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE);
							oVeinPacket.playerID = player.getEntityId();
							
							// Add the data packet into a NBTTagCompound
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setByteArray("MineVein", oVeinPacket.writePacketData().array());
							
							// Send the data packet onto Veinator for processing 
							FMLInterModComms.sendRuntimeMessage(Shaftanator.MODID, "superminer_veinator", "MineVein", nbt);
							
						} else {
							
							while (SuperMiner_Core.isMCTicking()) try { Thread.sleep(1); } catch (InterruptedException e) { }
							
							// Double check that the block isn't air
							if (!world.isAirBlock(workingPos)) {
								boolean bHarvested = player.interactionManager.tryHarvestBlock(workingPos);
	
								// Illuminate the new shaft if enabled and Illuminator is enabled
								if (bHarvested && this.bAutoIlluminate() && workingPos.getY() == this.iLowestY) {
									// Setup the Illuminator data packet
									IlluminatorPacket iPacket = new IlluminatorPacket();
									iPacket.oPos = new BlockPos (this.sideHit == EnumFacing.SOUTH ? workingPos.south() : 
																(this.sideHit == EnumFacing.NORTH ? workingPos.north() : 
																(this.sideHit == EnumFacing.EAST ? workingPos.east() : 
																(this.sideHit == EnumFacing.WEST ? workingPos.west() : 
																	workingPos))));
									iPacket.playerID = player.getEntityId();
									
									// Add the data packet into a NBTTagCompound
									NBTTagCompound nbt = new NBTTagCompound();
									nbt.setByteArray("IlluminateShaftData", iPacket.writePacketData().array());
									
									// Send the data packet onto Illuminator for processing 
									FMLInterModComms.sendRuntimeMessage(Shaftanator.MODID, "superminer_illuminator", "IlluminateShaft", nbt);
								}
							}
						}
					}
				}
		}

		if (oPositions.isEmpty()) bIsExcavating = false;
		
		return bIsExcavating;
	}
	
 	public void getExcavationBlocks() {
		oPositions.offer(oInitialPos);
		
		iLowestY = oInitialPos.getY();

		iBlocksFound = 1;
		boolean bHasConnected = false;
		
		BlockPos oPos;
					
		DirectionLoops:
		switch (this.sideHit) {
		case NORTH:	// Z = Depth ++ | X = Width | Y = Height
			for (int excavateZ = oInitialPos.getZ(); excavateZ <= (oInitialPos.getZ() + SettingsExcavator.iBlockRadius); excavateZ++) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int zOffset = (excavateZ != oInitialPos.getZ() ? -1 : 0); zOffset <= 1; zOffset++)
						for (int xOffset = -1; xOffset <= 1; xOffset++)
							for (int yOffset = (!bLayerOnlyToggled ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++) {
								oPos = new BlockPos(oInitialPos.getX() + xOffset, oInitialPos.getY() + yOffset, excavateZ + zOffset);
								
								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralNorthSouth(excavateZ)) break DirectionLoops;
			}
			break;
		case SOUTH:	// Z = Depth -- | X = Width | Y = Height
			for (int excavateZ = oInitialPos.getZ(); excavateZ >= (oInitialPos.getZ() - SettingsExcavator.iBlockRadius); excavateZ--) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int zOffset = (excavateZ != oInitialPos.getZ() ? -1 : 0); zOffset <= 1; zOffset++)
						for (int xOffset = -1; xOffset <= 1; xOffset++)
							for (int yOffset = (!bLayerOnlyToggled ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++) {
								oPos = new BlockPos(oInitialPos.getX() + xOffset, oInitialPos.getY() + yOffset, excavateZ - zOffset);

								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralNorthSouth(excavateZ)) break DirectionLoops;
			}
			break;
		case EAST:		// X = Depth -- | Z = Width | Y = Height
			for (int excavateX = oInitialPos.getX(); excavateX >= (oInitialPos.getX() - SettingsExcavator.iBlockRadius); excavateX--) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int xOffset = (excavateX != oInitialPos.getX() ? -1 : 0); xOffset <= 1; xOffset++)
						for (int zOffset = -1; zOffset <= 1; zOffset++)
							for (int yOffset = (!bLayerOnlyToggled ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++) {
								oPos = new BlockPos(excavateX - xOffset, oInitialPos.getY() + yOffset, oInitialPos.getZ() + zOffset);

								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralEastWest(excavateX)) break DirectionLoops;
			}
			break;
		case WEST:		// X = Depth ++ | Z = Width | Y = Height
			for (int excavateX = oInitialPos.getX(); excavateX <= (oInitialPos.getX() + SettingsExcavator.iBlockRadius); excavateX++) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int xOffset = (excavateX != oInitialPos.getX() ? -1 : 0); xOffset <= 1; xOffset++)
						for (int zOffset = -1; zOffset <= 1; zOffset++)
							for (int yOffset = (!bLayerOnlyToggled ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++) {
								oPos = new BlockPos(excavateX + xOffset, oInitialPos.getY() + yOffset, oInitialPos.getZ() + zOffset);

								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralEastWest(excavateX)) break DirectionLoops;
			}
			break;
		case UP:		// Y = Depth -- | X = Width | Z = Height
			for (int excavateY = oInitialPos.getY(); excavateY >= (oInitialPos.getY() - (!bLayerOnlyToggled ? SettingsExcavator.iBlockRadius : 0)); excavateY--) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int yOffset = (excavateY != oInitialPos.getY() ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++)
						for (int xOffset = -1; xOffset <= 1; xOffset++)
							for (int zOffset = -1; zOffset <= 1; zOffset++) {
								oPos = new BlockPos(oInitialPos.getX() + xOffset, excavateY - yOffset, oInitialPos.getZ() + zOffset);

								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralUpDown(excavateY)) break DirectionLoops;
			}
			break;
		case DOWN:	// Y = Depth ++ | X = Width | Z = Height
			for (int excavateY = oInitialPos.getY(); excavateY <= (oInitialPos.getY() + (!bLayerOnlyToggled ? SettingsExcavator.iBlockRadius : 0)); excavateY++) {
				// Ensure we have a direct connection.
				bHasConnected = false;
				
				ConnectionCheck:
				if (!bHasConnected)
					for (int yOffset = (excavateY != oInitialPos.getY() ? -1 : 0); yOffset <= (!bLayerOnlyToggled ? 1 : 0); yOffset++)
						for (int xOffset = -1; xOffset <= 1; xOffset++)
							for (int zOffset = -1; zOffset <= 1; zOffset++) {
								oPos = new BlockPos(oInitialPos.getX() + xOffset, excavateY - yOffset, oInitialPos.getZ() + zOffset);
								
								if (Globals.checkBlock(world.getBlockState(oPos), oPacket, SettingsExcavator.bDetectVariants)) {
									bHasConnected = true;
									break ConnectionCheck;
								}
							}
				
				if (!bHasConnected || !SpiralUpDown(excavateY)) break DirectionLoops;
			}
			break;
		}
	}
	
	/**
	 * Harvest Blocks in a spiral motion on the current Z axis.
	 * @param excavateZ = the current Z axis.
	 * @return true - false if total blocks found is greater than or equal to BlockLimit.
	 */
	private boolean SpiralNorthSouth(int excavateZ) {
		for (int iSpiral = 1; iSpiral <= (int)(SettingsExcavator.iBlockRadius / 2); iSpiral++) {
			iUnconnectedCount = 0;
			for (int xOffset = -iSpiral; xOffset <= iSpiral; xOffset++)
				for (int yOffset = (!bLayerOnlyToggled ? -iSpiral : 0); yOffset <= (!bLayerOnlyToggled ? iSpiral : 0); yOffset++)
					for (int zOffset = 0; zOffset <= (excavateZ == oInitialPos.getZ() ? 0 : 1); zOffset++) {
						BlockPos curPos = new BlockPos(oInitialPos.getX() + xOffset, oInitialPos.getY() + yOffset, excavateZ + (this.sideHit == EnumFacing.NORTH ? -zOffset : zOffset));
						if (Globals.checkBlock(world.getBlockState(curPos), oPacket, SettingsExcavator.bDetectVariants))
							AddCoordsToList(curPos);
						
						if (iBlocksFound >= SettingsExcavator.iBlockLimit) return false;
					}
		}
		return true;
	}
	
	/**
	 * Harvest Blocks in a spiral motion on the current X axis.
	 * @param excavateX = the current X axis.
	 * @return true - false if total blocks found is greater than or equal to BlockLimit.
	 */
	private boolean SpiralEastWest(int excavateX) {
		for (int iSpiral = 1; iSpiral <= (int)(SettingsExcavator.iBlockRadius / 2); iSpiral++) {
			iUnconnectedCount = 0;
			for (int zOffset = -iSpiral; zOffset <= iSpiral; zOffset++)
				for (int yOffset = (!bLayerOnlyToggled ? -iSpiral : 0); yOffset <= (!bLayerOnlyToggled ? iSpiral : 0); yOffset++)
					for (int xOffset = 0; xOffset <= (excavateX == oInitialPos.getX() ? 0 : 1); xOffset++) {
						BlockPos curPos = new BlockPos(excavateX + (this.sideHit == EnumFacing.EAST ? xOffset : -xOffset), oInitialPos.getY() + yOffset, oInitialPos.getZ() + zOffset);
						if (Globals.checkBlock(world.getBlockState(curPos), oPacket, SettingsExcavator.bDetectVariants))
							AddCoordsToList(curPos);
						
						if (iBlocksFound >= SettingsExcavator.iBlockLimit) return false;
					}
		}
		return true;
	}
	
	/**
	 * Harvest Blocks in a spiral motion on the current Y axis.
	 * @param excavateY = the current Y axis.
	 * @return true - false if total blocks found is greater than or equal to BlockLimit.
	 */
	private boolean SpiralUpDown(int excavateY) {
		for (int iSpiral = 1; iSpiral <= (int)(SettingsExcavator.iBlockRadius / 2); iSpiral++) {
			iUnconnectedCount = 0;
			for (int xOffset = -iSpiral; xOffset <= iSpiral; xOffset++)
				for (int zOffset = -iSpiral; zOffset <= iSpiral; zOffset++)
					for (int yOffset = 0; yOffset <= (excavateY == oInitialPos.getY() ? 0 : 1); yOffset++) {
						BlockPos curPos = new BlockPos(oInitialPos.getX() + xOffset, excavateY + (this.sideHit == EnumFacing.UP ? yOffset : -yOffset), oInitialPos.getZ() + zOffset);
						if (Globals.checkBlock(world.getBlockState(curPos), oPacket, SettingsExcavator.bDetectVariants))
							AddCoordsToList(curPos);
						
						if (iBlocksFound >= SettingsExcavator.iBlockLimit) return false;
					}
		}
		return true;
	}

	private void AddCoordsToList(BlockPos oPos) {
		boolean bValidBlock = false;
				
		if (!oPositions.contains(oPos)) {
			// Ensure that the current Block is connected to at least one of the previously found blocks
			ConnectionCheck:
			for (int xOffsetCheck = -1; xOffsetCheck <= 1; xOffsetCheck++)
				for (int zOffsetCheck = -1; zOffsetCheck <= 1; zOffsetCheck++)
					for (int yOffsetCheck = 1; yOffsetCheck >= -1; yOffsetCheck--)
						if (oPositions.contains(new BlockPos(oPos.getX() + xOffsetCheck, oPos.getY() + yOffsetCheck, oPos.getZ() + zOffsetCheck))) {
							bValidBlock = true;
							break ConnectionCheck;
						}
			
			if (bValidBlock) {
				oPositions.offer(oPos);
				
				if (oPos.getY() < this.iLowestY) this.iLowestY = oPos.getY();
				
				iBlocksFound++;
			} else
				iUnconnectedCount++;
		} else
			iUnconnectedCount++;
	}

	private static boolean isAllowedToMine(EntityPlayer player, Block block) {
		IBlockState state = block.getBlockState().getBaseState();
		if (null == block || Blocks.AIR == block || state.getMaterial().isLiquid() || Blocks.BEDROCK == block) return false;
		return player.canHarvestBlock(state);
	}
	
	public void getShaftBlocks(){
		oPositions.offer(oInitialPos);

		iFeetPos = (int)player.getEntityBoundingBox().minY;
		
		this.iLowestY = iFeetPos;
		
		int iHeightStart = iFeetPos;
		int iHeightEnd = iFeetPos + (SettingsShaftanator.iShaftHeight - 1);
		
		// if the ShaftWidth is divisible by 2 we don't want to do anything
		double dDivision = ((SettingsShaftanator.iShaftWidth & 1) != 0 ? 0 : 0.5);
		
		switch (oPacket.sideHit){
		case NORTH:
			iWidthStart = oPacket.oPos.getX() - ((int)(SettingsShaftanator.iShaftWidth/2));
			iWidthEnd = oPacket.oPos.getX() + ((int)((SettingsShaftanator.iShaftWidth/2)-dDivision));
			iLengthStart = oPacket.oPos.getZ();
			iLengthEnd = oPacket.oPos.getZ() + SettingsShaftanator.iShaftLength;
			break;
		case SOUTH:
			iWidthStart = oPacket.oPos.getX() + ((int)((SettingsShaftanator.iShaftWidth/2)-dDivision));
			iWidthEnd = oPacket.oPos.getX() - ((int)(SettingsShaftanator.iShaftWidth/2));
			iLengthStart = oPacket.oPos.getZ();
			iLengthEnd = oPacket.oPos.getZ() - SettingsShaftanator.iShaftLength;
			break;
		case WEST:
			iWidthStart = oPacket.oPos.getZ() - ((int)(SettingsShaftanator.iShaftWidth/2));
			iWidthEnd  = oPacket.oPos.getZ() + ((int)((SettingsShaftanator.iShaftWidth/2)-dDivision));
			iLengthStart = oPacket.oPos.getX();
			iLengthEnd = oPacket.oPos.getX() + SettingsShaftanator.iShaftLength;
			break;
		case EAST:
			iWidthStart = oPacket.oPos.getZ() + ((int)((SettingsShaftanator.iShaftWidth/2)-dDivision));
			iWidthEnd  = oPacket.oPos.getZ() - ((int)(SettingsShaftanator.iShaftWidth/2));
			iLengthStart = oPacket.oPos.getX();
			iLengthEnd = oPacket.oPos.getX() - SettingsShaftanator.iShaftLength;
			break;
		default:
			break;
		}

		int iAirCount = 0;
		
		BlockLoops:
		switch (oPacket.sideHit) {
		case NORTH:
		case WEST:
			for (int iLengthPos = iLengthStart; iLengthPos <= iLengthEnd; iLengthPos++) {
				iAirCount = 0;
				for (int iWidthPos = iWidthStart; iWidthPos <= iWidthEnd; iWidthPos++)
					for (int iHeightPos = iHeightStart; iHeightPos <= iHeightEnd; iHeightPos++) {
						BlockPos curPos = new BlockPos(	(oPacket.sideHit == EnumFacing.NORTH ? iWidthPos : iLengthPos), 
														iHeightPos, 
														(oPacket.sideHit == EnumFacing.NORTH ? iLengthPos : iWidthPos));
						
						if (isAllowedToMine(player, this.world.getBlockState(curPos).getBlock()))
							AddCoordsToList(curPos);
						else
							iAirCount++;
						
						if (iAirCount >= (SettingsShaftanator.iShaftHeight * SettingsShaftanator.iShaftWidth)) break BlockLoops;
					}
			}
			break;
		case SOUTH:
		case EAST:
			for (int iLengthPos = iLengthStart; iLengthPos >= iLengthEnd; iLengthPos--) {
				iAirCount = 0;
				for (int iWidthPos = iWidthStart; iWidthPos >= iWidthEnd; iWidthPos--)
					for (int iHeightPos = iHeightStart; iHeightPos <= iHeightEnd; iHeightPos++) {
						BlockPos curPos = new BlockPos(	(oPacket.sideHit == EnumFacing.SOUTH ? iWidthPos : iLengthPos), 
														iHeightPos, 
														(oPacket.sideHit == EnumFacing.SOUTH ? iLengthPos : iWidthPos));
						
						if (isAllowedToMine(player, this.world.getBlockState(curPos).getBlock()))
							AddCoordsToList(curPos);
						else
							iAirCount++;
						
						if (iAirCount >= (SettingsShaftanator.iShaftHeight * SettingsShaftanator.iShaftWidth)) break BlockLoops;
					}
			}
			break;
		default:
			break;
		}
	}

	public void FinalizeExcavation() {
//		Globals.stackItems(world, player, 
//				AxisAlignedBB.fromBounds(	oInitialPos.getX(), oInitialPos.getY(), oInitialPos.getZ(), 
//											oInitialPos.getX(), oInitialPos.getY(), oInitialPos.getZ()
//										).expand(SettingsExcavator.iBlockRadius + 2, SettingsExcavator.iBlockRadius + 2, SettingsExcavator.iBlockRadius + 2));
		
		if (SettingsExcavator.bGatherDrops) {
			List<Entity> list = Globals.getNearbyEntities(world, player.getEntityBoundingBox().expand(SettingsExcavator.iBlockRadius + 2, SettingsExcavator.iBlockRadius + 2, SettingsExcavator.iBlockRadius + 2));
			if (null != list && !list.isEmpty())
				for (Entity entity : list)
					if (!entity.isDead)
						entity.setPosition(oInitialPos.getX(), oInitialPos.getY(), oInitialPos.getZ());
		}
	}
	
	public void FinalizeShaft() {
//		Globals.stackItems(	world, player, 
//				AxisAlignedBB.fromBounds(	(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH 
//												? iWidthStart : iLengthStart), 
//											iFeetPos, 
//											(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH 
//													? iLengthStart : iWidthStart), 
//											(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH 
//													? iWidthEnd : iLengthEnd),
//											iFeetPos + SettingsShaftanator.iShaftHeight, 
//											(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH 
//													? iLengthEnd : iWidthEnd)));
		
		if (SettingsShaftanator.bGatherDrops) {
			List<Entity> list = Globals.getNearbyEntities(world, player.getEntityBoundingBox().expand(	(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH
																											? SettingsShaftanator.iShaftWidth + 2 : SettingsShaftanator.iShaftLength + 4), 
																										SettingsShaftanator.iShaftHeight + 2, 
																										(oPacket.sideHit == EnumFacing.NORTH || oPacket.sideHit == EnumFacing.SOUTH 
																											? SettingsShaftanator.iShaftLength + 4 : SettingsShaftanator.iShaftWidth + 2)));
			if (null != list && !list.isEmpty())
				for (Entity entity : list)
					if (!entity.isDead)
						entity.setPosition(oInitialPos.getX(), oInitialPos.getY(), oInitialPos.getZ());
		}	}
}
