package duelmonster.superminer.keys;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.config.*;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.submods.*;

public class KeyInputHandler {
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		// checking inGameHasFocus prevents your keys from firing when the player is typing a chat message
		// NOTE that the KeyInputEvent will NOT be posted when a gui screen such as the inventory is open
		if (FMLClientHandler.instance().getClient().inGameHasFocus) {
			
			if (KeyBindings.captivator.isPressed()) {
				SettingsCaptivator.bEnabled = !SettingsCaptivator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Captivator.MODID).get(Globals.localize("superminer.captivator.enabled")).setValue(SettingsCaptivator.bEnabled);
				Globals.NotifyClient(SettingsCaptivator.bEnabled, Captivator.MODName);
			}
			
			if (KeyBindings.excavator.isPressed()) {
				SettingsExcavator.bEnabled = !SettingsExcavator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Excavator.MODID).get(Globals.localize("superminer.excavator.enabled")).setValue(SettingsExcavator.bEnabled);
				Excavator.myGlobals.clearHistory();
				Globals.NotifyClient(SettingsExcavator.bEnabled, Excavator.MODName);
			}
			
			if (KeyBindings.illuminator.isPressed()) {
				SettingsIlluminator.bEnabled = !SettingsIlluminator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Illuminator.MODID).get(Globals.localize("superminer.illuminator.enabled")).setValue(SettingsIlluminator.bEnabled);
				Globals.NotifyClient(SettingsIlluminator.bEnabled, Illuminator.MODName);
			}
			
			if (KeyBindings.lumbinator.isPressed()) {
				SettingsLumbinator.bEnabled = !SettingsLumbinator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Lumbinator.MODID).get(Globals.localize("superminer.lumbinator.enabled")).setValue(SettingsLumbinator.bEnabled);
				Lumbinator.myGlobals.clearHistory();
				Globals.NotifyClient(SettingsLumbinator.bEnabled, Lumbinator.MODName);
			}
			
			if (KeyBindings.shaftanator.isPressed()) {
				SettingsShaftanator.bEnabled = !SettingsShaftanator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Shaftanator.MODID).get(Globals.localize("superminer.shaftanator.enabled")).setValue(SettingsShaftanator.bEnabled);
				Globals.NotifyClient(SettingsShaftanator.bEnabled, Shaftanator.MODName);
			}
			
			if (KeyBindings.substitutor.isPressed()) {
				SettingsSubstitutor.bEnabled = !SettingsSubstitutor.bEnabled;
				SuperMiner_Core.configFile.getCategory(Substitutor.MODID).get(Globals.localize("superminer.substitutor.enabled")).setValue(SettingsSubstitutor.bEnabled);
				Globals.NotifyClient(SettingsSubstitutor.bEnabled, Substitutor.MODName);
			}
			
			if (KeyBindings.veinator.isPressed()) {
				SettingsVeinator.bEnabled = !SettingsVeinator.bEnabled;
				SuperMiner_Core.configFile.getCategory(Veinator.MODID).get(Globals.localize("superminer.veinator.enabled")).setValue(SettingsVeinator.bEnabled);
				Veinator.myGlobals.clearHistory();
				Globals.NotifyClient(SettingsVeinator.bEnabled, Veinator.MODName);
			}
			
			if (SuperMiner_Core.configFile.hasChanged()) SuperMiner_Core.configFile.save();
		}
	}
}