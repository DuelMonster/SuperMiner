package duelmonster.superminer;

import java.io.File;

import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.intergration.ModSupport;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.keys.KeyInputHandler;
import duelmonster.superminer.submods.Captivator;
import duelmonster.superminer.submods.Cropinator;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Illuminator;
import duelmonster.superminer.submods.Lumbinator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Substitutor;
import duelmonster.superminer.submods.Veinator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(   modid = SuperMiner_Core.MODID
	  , name = SuperMiner_Core.MODName
	  , version = SuperMiner_Core.VERSION
	  , acceptedMinecraftVersions = SuperMiner_Core.MCVERSION
	  , dependencies = "after:*"
	  , guiFactory = "duelmonster.superminer.config.SMGuiFactory"
	  , updateJSON = SuperMiner_Core.FVC_URL
	)
public class SuperMiner_Core {
	public static final String MODID = "superminer_core";
	public static final String MODName = "SuperMiner";
    public static final String MODVERSION = "${mod_version}";
    public static final String MCVERSION = "[1.9]";
    public static final String VERSION = MCVERSION + "-" + MODVERSION;
        
	public static final String VC_URL = "http://www.duelmonster.talktalk.net/Minecraft/SuperMiner/mod_version.json";
	public static final String FVC_URL = "http://www.duelmonster.talktalk.net/Minecraft/SuperMiner/forge_update.json";

	public static Configuration configFile;

	@SidedProxy(clientSide="duelmonster.superminer.SM_Client", serverSide="duelmonster.superminer.SM_Proxy")
	public static SM_Proxy proxy;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
	    configFile = new Configuration(new File(e.getModConfigurationDirectory() + "/SuperMiner.cfg"), MODVERSION);
	    
		if (FMLCommonHandler.instance().getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(new PlayerEvents());

			MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
			KeyBindings.init();
		}
		
		proxy.registerEventHandler();
				
		MinecraftForge.EVENT_BUS.register(this);

		FMLInterModComms.sendRuntimeMessage(MODID, "VersionChecker", "addVersionCheck", VC_URL);
	}

	@Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        
        loadConfigData();
		ModSupport.addModSupport();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(SuperMiner_Core.MODID)) {
			SuperMiner_Core.configFile.save();
			loadConfigData();
		}
	}

	private void loadConfigData() {
		Captivator.syncConfig();
		Cropinator.syncConfig();
		Excavator.syncConfig();
		Illuminator.syncConfig();
		Lumbinator.syncConfig();
		Shaftanator.syncConfig();
		Substitutor.syncConfig();
		Veinator.syncConfig();
	}
	
	private static boolean bIsClientTicking = false;
	private static boolean bIsPlayerTicking = false;
	private static boolean bIsServerTicking = false;
	private static boolean bIsWorldTicking = false;
	
	public static boolean isMCTicking() {
		if (FMLCommonHandler.instance().getSide().isClient())
			return (bIsClientTicking || bIsPlayerTicking || bIsServerTicking || bIsWorldTicking);
		else
			return (bIsPlayerTicking || bIsWorldTicking);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (TickEvent.Phase.START.equals(event.phase)) bIsClientTicking = true;
		else if (TickEvent.Phase.END.equals(event.phase)) bIsClientTicking = false;
	}
	@SubscribeEvent
	public void tickEvent(TickEvent.PlayerTickEvent event) {
		if (TickEvent.Phase.START.equals(event.phase)) bIsPlayerTicking = true;
		else if (TickEvent.Phase.END.equals(event.phase)) bIsPlayerTicking = false;
	}
	@SubscribeEvent
	public void tickEvent(TickEvent.ServerTickEvent event) {
		if (TickEvent.Phase.START.equals(event.phase)) bIsServerTicking = true;
		else if (TickEvent.Phase.END.equals(event.phase)) bIsServerTicking = false;
	}
	@SubscribeEvent
	public void tickEvent(TickEvent.WorldTickEvent event) {
		if (TickEvent.Phase.START.equals(event.phase)) bIsWorldTicking = true;
		else if (TickEvent.Phase.END.equals(event.phase)) bIsWorldTicking = false;
	}
}
