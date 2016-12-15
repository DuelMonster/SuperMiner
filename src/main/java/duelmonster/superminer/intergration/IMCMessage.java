package duelmonster.superminer.intergration;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

/**
 * Helper functions to send intermod communication messages
 */
public class IMCMessage {
	private final static String MOD = "superminer_";

	public static void addTool(ToolType toolType, String toolID) {
		String sModID = MOD;
		String sToolType = "";
		
		switch (toolType) {
		case axe:
			sToolType = "Axe";
			sModID += "lumbinator";
			break;
		case hoe:
			sToolType = "Hoe";
			sModID += "excavator";
			break;
		case pickaxe:
			sToolType = "Pickaxe";
			sModID += "veinator";
			break;
		case shovel:
			sToolType = "Shovel";
			sModID += "excavator";
			break;
		case shears:
//			sToolType = "Shears";
//			sModID += "excavator";
			break;
		default:
			break;		
		}
		
		if (sModID != MOD && Loader.isModLoaded(sModID))			
			FMLInterModComms.sendMessage(sModID, "add" + sToolType, toolID);
	}
	
	public static void addBlock(Material blockType, String blockName) {
		String sModID = MOD;
		String sBlockType = "";
		
		if (blockType == Material.WOOD) {
			sModID += "lumbinator";
			sBlockType = "Wood";
		} else if (blockType == Material.LEAVES) {
			sModID += "lumbinator";
			sBlockType = "Leaves";
		}
		
		if (sModID != MOD && Loader.isModLoaded(sModID)) FMLInterModComms.sendMessage(sModID, "add" + sBlockType, blockName);
	}

	public static void addOre(String blockName) {
		String sModID = MOD + "veinator";
		
		if (sModID != MOD && Loader.isModLoaded(sModID)) FMLInterModComms.sendMessage(sModID, "addOre", blockName);
	}

}
