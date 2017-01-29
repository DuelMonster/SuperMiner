package duelmonster.superminer.config;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.submods.Captivator;
import duelmonster.superminer.submods.Cropinator;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Illuminator;
import duelmonster.superminer.submods.Lumbinator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Substitutor;
import duelmonster.superminer.submods.Veinator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

public class SMGuiConfig extends GuiConfig {
	public SMGuiConfig(GuiScreen parentScreen) {
		super(parentScreen,
				getConfigElements(),
				SuperMiner_Core.MODID,
				false,
				false,
				"SuperMiner Configuration");
	}
	
	/** Compiles a list of config elements */
	@SuppressWarnings("rawtypes")
	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		
		// Add categories to config GUI
		list.add(categoryElement(Captivator.MODID, Captivator.MODName, "superminer." + Captivator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Cropinator.MODID, Cropinator.MODName, "superminer." + Cropinator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Excavator.MODID, Excavator.MODName, "superminer." + Excavator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Illuminator.MODID, Illuminator.MODName, "superminer." + Illuminator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Lumbinator.MODID, Lumbinator.MODName, "superminer." + Lumbinator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Shaftanator.MODID, Shaftanator.MODName, "superminer." + Shaftanator.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Substitutor.MODID, Substitutor.MODName, "superminer." + Substitutor.MODName.toLowerCase() + ".config"));
		list.add(categoryElement(Veinator.MODID, Veinator.MODName, "superminer." + Veinator.MODName.toLowerCase() + ".config"));
		
		return list;
	}
	
	/** Creates a button linking to another screen where all options of the category are available */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static IConfigElement categoryElement(String category, String name, String tooltip_key) {
		return new DummyConfigElement.DummyCategoryElement(name, tooltip_key, new ConfigElement(SuperMiner_Core.configFile.getCategory(category)).getChildElements());
	}
}
