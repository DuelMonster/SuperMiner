package duelmonster.superminer.keys;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;

public class KeyBindings {
	
	// Declare our KeyBindings
	public static KeyBinding captivator;
	
	public static KeyBinding	excavator;
	public static KeyBinding	excavator_toggle;
	public static KeyBinding	excavator_layer_only_toggle;
	
	public static KeyBinding	illuminator;
	public static KeyBinding	illuminator_place;
	
	public static KeyBinding lumbinator;
	
	public static KeyBinding	shaftanator;
	public static KeyBinding	shaftanator_toggle;
	
	public static KeyBinding substitutor;
	
	public static KeyBinding veinator;
	
	public static void init() {
		// Define the bindings, with unlocalised names "key.???" and
		// the unlocalised category name "key.categories.superminer"
		
		// default key code for Captivator
		captivator = new KeyBinding("superminer.captivator.enabled.desc", Keyboard.KEY_NUMPAD1, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(captivator);
		
		// default key code for Enabling/Disabling Excavator
		excavator = new KeyBinding("superminer.excavator.enabled.desc", Keyboard.KEY_NUMPAD2, "SuperMiner");
		
		// default key code for Excavator Toggle = "`", LWJGL constant: Keyboard.KEY_GRAVE
		excavator_toggle = new KeyBinding("superminer.excavator.toggle", Keyboard.KEY_GRAVE, "SuperMiner");
		
		// default key code for Excavator Single Layer Toggle = "\", LWJGL constant: Keyboard.KEY_BACKSLASH
		excavator_layer_only_toggle = new KeyBinding("superminer.excavator.toggle.sl", Keyboard.KEY_BACKSLASH, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(excavator);
		ClientRegistry.registerKeyBinding(excavator_toggle);
		ClientRegistry.registerKeyBinding(excavator_layer_only_toggle);
		
		// default key code for Illuminator
		illuminator = new KeyBinding("superminer.illuminator.enabled.desc", Keyboard.KEY_NUMPAD3, "SuperMiner");
		
		// default key code for Illuminator Torch Placing = "V", LWJGL constant: Keyboard.KEY_V
		illuminator_place = new KeyBinding("superminer.illuminator.place", Keyboard.KEY_V, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(illuminator);
		ClientRegistry.registerKeyBinding(illuminator_place);
		
		// default key code for Lumbinator
		lumbinator = new KeyBinding("superminer.lumbinator.enabled.desc", Keyboard.KEY_NUMPAD4, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(lumbinator);
		
		// default key code for Shaftanator
		shaftanator = new KeyBinding("superminer.shaftanator.enabled.desc", Keyboard.KEY_NUMPAD5, "SuperMiner");
		
		// default key code for Shaftanator Toggle = "'"
		shaftanator_toggle = new KeyBinding("superminer.shaftanator.toggle", Keyboard.KEY_LMENU, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(shaftanator);
		ClientRegistry.registerKeyBinding(shaftanator_toggle);
		
		// default key code for Substitutor
		substitutor = new KeyBinding("superminer.substitutor.enabled.desc", Keyboard.KEY_NUMPAD6, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(substitutor);
		
		// default key code for Veinator
		veinator = new KeyBinding("superminer.veinator.enabled.desc", Keyboard.KEY_NUMPAD7, "SuperMiner");
		
		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(veinator);
	}
	
}
