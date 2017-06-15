package duelmonster.superminer.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class SMGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {}
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return SMGuiConfig.class;
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new SMGuiConfig(parentScreen);
	}

	@SuppressWarnings("deprecation")
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		// TODO Auto-generated method stub
		return null;
	}
}
