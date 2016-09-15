package duelmonster.superminer.intergration;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Loader;

public class ModSupport {

	public static void addModSupport() {
		
		if(Loader.isModLoaded("IC2")) {
			IMCMessage.addTool(ToolType.axe, "IC2:itemToolBronzeAxe");
			IMCMessage.addTool(ToolType.axe, "IC2:itemToolChainsaw");
			
			IMCMessage.addTool(ToolType.hoe, "IC2:itemToolBronzeHoe");
			
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolBronzePickaxe");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolDrill");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolDDrill");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolIridiumDrill");
			
			IMCMessage.addTool(ToolType.shovel, "IC2:itemToolBronzeSpade");

			IMCMessage.addTool(ToolType.shears, "IC2:itemToolBronzeHoe");
			
			IMCMessage.addBlock(Material.wood, "IC2:blockRubWood");
			IMCMessage.addBlock(Material.leaves, "IC2:blockRubLeaves");
		}
		
		if(Loader.isModLoaded("appliedenergistics2")) {
			IMCMessage.addTool(ToolType.axe, "appliedenergistics2:item.ToolCertusQuartzAxe");
			IMCMessage.addTool(ToolType.axe, "appliedenergistics2:item.ToolNetherQuartzAxe");
			
			IMCMessage.addTool(ToolType.hoe, "appliedenergistics2:item.ToolCertusQuartzHoe");
			IMCMessage.addTool(ToolType.hoe, "appliedenergistics2:item.ToolNetherQuartzHoe");
			
			IMCMessage.addTool(ToolType.pickaxe, "appliedenergistics2:item.ToolCertusQuartzPickaxe");
			IMCMessage.addTool(ToolType.pickaxe, "appliedenergistics2:item.ToolNetherQuartzPickaxe");

			IMCMessage.addTool(ToolType.shovel, "appliedenergistics2:item.ToolCertusQuartzSpade");
			IMCMessage.addTool(ToolType.shovel, "appliedenergistics2:item.ToolNetherQuartzSpade");
		}
		
		if(Loader.isModLoaded("BiomesOPlenty")) {
			IMCMessage.addTool(ToolType.axe, "BiomesOPlenty:mud_axe");
			IMCMessage.addTool(ToolType.axe, "BiomesOPlenty:amethyst_axe");

			IMCMessage.addTool(ToolType.hoe, "BiomesOPlenty:mud_hoe");
			IMCMessage.addTool(ToolType.hoe, "BiomesOPlenty:amethyst_hoe");

			IMCMessage.addTool(ToolType.pickaxe, "BiomesOPlenty:mud_pickaxe");
			IMCMessage.addTool(ToolType.pickaxe, "BiomesOPlenty:amethyst_pickaxe");
			
			IMCMessage.addTool(ToolType.shovel, "BiomesOPlenty:mud_shovel");
			IMCMessage.addTool(ToolType.shovel, "BiomesOPlenty:amethyst_shovel");

			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:mud_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:wood_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:stone_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:iron_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:gold_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:amethyst_scythe");
			IMCMessage.addTool(ToolType.shears, "BiomesOPlenty:diamond_scythe");

			IMCMessage.addBlock(Material.wood, "BiomesOPlenty:log_0");
			IMCMessage.addBlock(Material.wood, "BiomesOPlenty:log_1");
			IMCMessage.addBlock(Material.wood, "BiomesOPlenty:log_2");
			IMCMessage.addBlock(Material.wood, "BiomesOPlenty:log_3");
			IMCMessage.addBlock(Material.wood, "BiomesOPlenty:log_4");
			
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_0");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_1");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_2");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_3");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_4");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_5");
			IMCMessage.addBlock(Material.leaves, "BiomesOPlenty:leaves_6");
		}
		
		if(Loader.isModLoaded("TConstruct")) {
			IMCMessage.addTool(ToolType.axe, "TConstruct:hatchet");

			IMCMessage.addTool(ToolType.hoe, "TConstruct:mattock");
			
			IMCMessage.addTool(ToolType.pickaxe, "TConstruct:pickaxe");
			
			IMCMessage.addTool(ToolType.shovel, "TConstruct:shovel");
			IMCMessage.addTool(ToolType.shovel, "TConstruct:mattock");
		}

		if(Loader.isModLoaded("Natura")) {
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.bloodwood");
	        IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.darkwood");
	        IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.fusewood");
	        IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.ghostwood");
	        IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.netherquartz");

			IMCMessage.addBlock(Material.wood, "Natura:tree");
			IMCMessage.addBlock(Material.leaves, "Natura:floraleaves");
			IMCMessage.addBlock(Material.leaves, "Natura:floraleavesnocolor");
		}

		if(Loader.isModLoaded("Thaumcraft")) {
			IMCMessage.addTool(ToolType.axe, "Thaumcraft:ItemAxeThaumium");
			IMCMessage.addTool(ToolType.axe, "Thaumcraft:ItemAxeElemental");

			IMCMessage.addBlock(Material.wood, "Thaumcraft:blockMagicalLog");
			IMCMessage.addBlock(Material.leaves, "Thaumcraft:blockMagicalLeaves");
		}
		
		if(Loader.isModLoaded("TwilightForest")) {
			IMCMessage.addTool(ToolType.axe, "TwilightForest:item.ironwoodAxe");
	        IMCMessage.addTool(ToolType.axe, "TwilightForest:item.knightlyAxe");
	        IMCMessage.addTool(ToolType.axe, "TwilightForest:item.minotaurAxe");
	        IMCMessage.addTool(ToolType.axe, "TwilightForest:item.steeleafAxe");

	        IMCMessage.addBlock(Material.wood, "TwilightForest:tile.TFLog");
	        IMCMessage.addBlock(Material.wood, "TwilightForest:tile.TFMagicLog");
	        IMCMessage.addBlock(Material.wood, "TwilightForest:tile.TFMagicLogSpecial");
	        
	        IMCMessage.addBlock(Material.leaves, "TwilightForest:tile.TFLeaves");
	        IMCMessage.addBlock(Material.leaves, "TwilightForest:tile.TFHedge");
	        IMCMessage.addBlock(Material.leaves, "TwilightForest:tile.TFMagicLeaves");
		}

		if(Loader.isModLoaded("exnihilo")) {
			IMCMessage.addTool(ToolType.crook, "exnihilo:crook");
			IMCMessage.addTool(ToolType.crook, "exnihilo:crook_bone");

			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_wood");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_stone");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_iron");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_gold");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_diamond");

//			IMCMessage.addBlock(Material.leaves, "minecraft:leaves");
//			IMCMessage.addBlock(Material.leaves, "minecraft:leaves2");
//			
//			IMCMessage.addBlock(Material.plant, "minecraft:tallgrass");
//			IMCMessage.addBlock(Material.plant, "minecraft:vine");
//			
//			IMCMessage.addBlock(Material.cobweb, "minecraft:web");
//			
//			IMCMessage.addBlock(Material.wool, "minecraft:wool");
			
			IMCMessage.addBlock(Material.sand, "exnihilo:aluminum_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:aluminum_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:aluminum_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:copper_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:copper_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:copper_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:ender_lead_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:ender_platinum_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:ender_silver_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:ender_tin_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:exnihilo.gravel_ender");
			IMCMessage.addBlock(Material.sand, "exnihilo:exnihilo.gravel_nether");
			IMCMessage.addBlock(Material.sand, "exnihilo:gold_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:gold_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:gold_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:iron_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:iron_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:iron_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:lead_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:lead_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:lead_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:nether_copper_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:nether_gold_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:nether_iron_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:nether_nickel_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:nickel_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:nickel_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:nickel_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:platinum_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:platinum_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:platinum_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:silver_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:silver_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:silver_sand");
			IMCMessage.addBlock(Material.sand, "exnihilo:tin_dust");
			IMCMessage.addBlock(Material.sand, "exnihilo:tin_gravel");
			IMCMessage.addBlock(Material.sand, "exnihilo:tin_sand");
		}
		
		if(Loader.isModLoaded("excompressum")) {
			IMCMessage.addTool(ToolType.hammer, "excompressum:chickenStick");
		}
	}

}
