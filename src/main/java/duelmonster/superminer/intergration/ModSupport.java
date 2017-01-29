package duelmonster.superminer.intergration;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Loader;

public class ModSupport {
	
	public static void addModSupport() {
		
		if (Loader.isModLoaded("IC2")) {
			IMCMessage.addTool(ToolType.axe, "IC2:itemToolBronzeAxe");
			IMCMessage.addTool(ToolType.axe, "IC2:itemToolChainsaw");
			
			IMCMessage.addTool(ToolType.hoe, "IC2:itemToolBronzeHoe");
			
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolBronzePickaxe");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolDrill");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolDDrill");
			IMCMessage.addTool(ToolType.pickaxe, "IC2:itemToolIridiumDrill");
			
			IMCMessage.addTool(ToolType.shovel, "IC2:itemToolBronzeSpade");
			
			IMCMessage.addTool(ToolType.shears, "IC2:itemToolBronzeHoe");
			
			IMCMessage.addBlock(Material.WOOD, "IC2:blockRubWood");
			IMCMessage.addBlock(Material.LEAVES, "IC2:blockRubLeaves");
		}
		
		if (Loader.isModLoaded("appliedenergistics2")) {
			IMCMessage.addTool(ToolType.axe, "appliedenergistics2:item.ToolCertusQuartzAxe");
			IMCMessage.addTool(ToolType.axe, "appliedenergistics2:item.ToolNetherQuartzAxe");
			
			IMCMessage.addTool(ToolType.hoe, "appliedenergistics2:item.ToolCertusQuartzHoe");
			IMCMessage.addTool(ToolType.hoe, "appliedenergistics2:item.ToolNetherQuartzHoe");
			
			IMCMessage.addTool(ToolType.pickaxe, "appliedenergistics2:item.ToolCertusQuartzPickaxe");
			IMCMessage.addTool(ToolType.pickaxe, "appliedenergistics2:item.ToolNetherQuartzPickaxe");
			
			IMCMessage.addTool(ToolType.shovel, "appliedenergistics2:item.ToolCertusQuartzSpade");
			IMCMessage.addTool(ToolType.shovel, "appliedenergistics2:item.ToolNetherQuartzSpade");
		}
		
		if (Loader.isModLoaded("BiomesOPlenty")) {
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
			
			IMCMessage.addBlock(Material.WOOD, "BiomesOPlenty:log_0");
			IMCMessage.addBlock(Material.WOOD, "BiomesOPlenty:log_1");
			IMCMessage.addBlock(Material.WOOD, "BiomesOPlenty:log_2");
			IMCMessage.addBlock(Material.WOOD, "BiomesOPlenty:log_3");
			IMCMessage.addBlock(Material.WOOD, "BiomesOPlenty:log_4");
			
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_0");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_1");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_2");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_3");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_4");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_5");
			IMCMessage.addBlock(Material.LEAVES, "BiomesOPlenty:leaves_6");
		}
		
		if (Loader.isModLoaded("TConstruct")) {
			IMCMessage.addTool(ToolType.axe, "TConstruct:hatchet");
			
			IMCMessage.addTool(ToolType.hoe, "TConstruct:mattock");
			
			IMCMessage.addTool(ToolType.pickaxe, "TConstruct:pickaxe");
			
			IMCMessage.addTool(ToolType.shovel, "TConstruct:shovel");
			IMCMessage.addTool(ToolType.shovel, "TConstruct:mattock");
		}
		
		if (Loader.isModLoaded("Natura")) {
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.bloodwood");
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.darkwood");
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.fusewood");
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.ghostwood");
			IMCMessage.addTool(ToolType.axe, "Natura:natura.axe.netherquartz");
			
			IMCMessage.addBlock(Material.WOOD, "Natura:tree");
			IMCMessage.addBlock(Material.LEAVES, "Natura:floraleaves");
			IMCMessage.addBlock(Material.LEAVES, "Natura:floraleavesnocolor");
		}
		
		if (Loader.isModLoaded("Thaumcraft")) {
			IMCMessage.addTool(ToolType.axe, "Thaumcraft:ItemAxeThaumium");
			IMCMessage.addTool(ToolType.axe, "Thaumcraft:ItemAxeElemental");
			
			IMCMessage.addBlock(Material.WOOD, "Thaumcraft:blockMagicalLog");
			IMCMessage.addBlock(Material.LEAVES, "Thaumcraft:blockMagicalLeaves");
		}
		
		if (Loader.isModLoaded("TwilightForest")) {
			IMCMessage.addTool(ToolType.axe, "TwilightForest:item.ironwoodAxe");
			IMCMessage.addTool(ToolType.axe, "TwilightForest:item.knightlyAxe");
			IMCMessage.addTool(ToolType.axe, "TwilightForest:item.minotaurAxe");
			IMCMessage.addTool(ToolType.axe, "TwilightForest:item.steeleafAxe");
			
			IMCMessage.addBlock(Material.WOOD, "TwilightForest:tile.TFLog");
			IMCMessage.addBlock(Material.WOOD, "TwilightForest:tile.TFMagicLog");
			IMCMessage.addBlock(Material.WOOD, "TwilightForest:tile.TFMagicLogSpecial");
			
			IMCMessage.addBlock(Material.LEAVES, "TwilightForest:tile.TFLeaves");
			IMCMessage.addBlock(Material.LEAVES, "TwilightForest:tile.TFHedge");
			IMCMessage.addBlock(Material.LEAVES, "TwilightForest:tile.TFMagicLeaves");
		}
		
		if (Loader.isModLoaded("exnihilo")) {
			IMCMessage.addTool(ToolType.crook, "exnihilo:crook");
			IMCMessage.addTool(ToolType.crook, "exnihilo:crook_bone");
			
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_wood");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_stone");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_iron");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_gold");
			IMCMessage.addTool(ToolType.hammer, "exnihilo:hammer_diamond");
			
			// IMCMessage.addBlock(Material.leaves, "minecraft:leaves");
			// IMCMessage.addBlock(Material.leaves, "minecraft:leaves2");
			//
			// IMCMessage.addBlock(Material.plant, "minecraft:tallgrass");
			// IMCMessage.addBlock(Material.plant, "minecraft:vine");
			//
			// IMCMessage.addBlock(Material.cobweb, "minecraft:web");
			//
			// IMCMessage.addBlock(Material.wool, "minecraft:wool");
			
			IMCMessage.addBlock(Material.SAND, "exnihilo:aluminum_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:aluminum_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:aluminum_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:copper_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:copper_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:copper_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:ender_lead_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:ender_platinum_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:ender_silver_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:ender_tin_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:exnihilo.gravel_ender");
			IMCMessage.addBlock(Material.SAND, "exnihilo:exnihilo.gravel_nether");
			IMCMessage.addBlock(Material.SAND, "exnihilo:gold_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:gold_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:gold_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:iron_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:iron_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:iron_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:lead_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:lead_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:lead_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nether_copper_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nether_gold_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nether_iron_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nether_nickel_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nickel_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nickel_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:nickel_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:platinum_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:platinum_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:platinum_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:silver_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:silver_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:silver_sand");
			IMCMessage.addBlock(Material.SAND, "exnihilo:tin_dust");
			IMCMessage.addBlock(Material.SAND, "exnihilo:tin_gravel");
			IMCMessage.addBlock(Material.SAND, "exnihilo:tin_sand");
		}
		
		if (Loader.isModLoaded("excompressum")) {
			IMCMessage.addTool(ToolType.hammer, "excompressum:chickenStick");
		}
		
		if (Loader.isModLoaded("mysticalagriculture")) {
			IMCMessage.addOre("mysticalagriculture:prosperity_ore");
			IMCMessage.addOre("mysticalagriculture:nether_prosperity_ore");
			IMCMessage.addOre("mysticalagriculture:end_prosperity_ore");
			IMCMessage.addOre("mysticalagriculture:inferium_ore");
			IMCMessage.addOre("mysticalagriculture:nether_inferium_ore");
			IMCMessage.addOre("mysticalagriculture:end_inferium_ore");
		}
		
		if (Loader.isModLoaded("mekanism")) {
			IMCMessage.addOre("mekanism:OreBlock");
		}
		
		if (Loader.isModLoaded("quantumflux")) {
			IMCMessage.addOre("quantumflux:graphiteOre");
		}
	}
}
