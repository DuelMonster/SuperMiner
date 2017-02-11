package duelmonster.superminer;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import duelmonster.superminer.events.PlayerEvents;
import duelmonster.superminer.intergration.ModSupport;
import duelmonster.superminer.keys.KeyBindings;
import duelmonster.superminer.keys.KeyInputHandler;
import duelmonster.superminer.objects.ExcavationHelper;
import duelmonster.superminer.proxy.SM_Proxy;
import duelmonster.superminer.submods.Captivator;
import duelmonster.superminer.submods.Cropinator;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Illuminator;
import duelmonster.superminer.submods.Lumbinator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Substitutor;
import duelmonster.superminer.submods.Veinator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(	modid = SuperMiner_Core.MODID,
		name = SuperMiner_Core.MODName,
		version = SuperMiner_Core.MODVERSION,
		acceptedMinecraftVersions = SuperMiner_Core.MCVERSION,
		dependencies = "after:*",
		guiFactory = "duelmonster.superminer.config.SMGuiFactory",
		updateJSON = SuperMiner_Core.FVC_URL)
public class SuperMiner_Core {
	public static final String	MODID		= "superminer_core";
	public static final String	MODName		= "SuperMiner";
	public static final String	MODVERSION	= "${mod_version}";
	public static final String	MCVERSION	= "[1.10.2]";
	public static final String	VERSION		= MCVERSION + "-" + MODVERSION;
	
	public static final String FVC_URL = "http://www.duelmonster.talktalk.net/Minecraft/SuperMiner/forge_update.json";
	
	public static Configuration configFile;
	
	@SidedProxy(clientSide = "duelmonster.superminer.proxy.SM_Client",
				serverSide = "duelmonster.superminer.proxy.SM_Proxy")
	public static SM_Proxy proxy;
	
	public static Logger LOGGER = LogManager.getLogger(MODName);
	
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
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		
		if (FMLCommonHandler.instance().getSide().isClient())
			loadConfigData();
		
		ModSupport.addModSupport();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(SuperMiner_Core.MODID)) {
			SuperMiner_Core.configFile.save();
			loadConfigData();
		}
	}
	
	public static ExcavationHelper ehWorker = null;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote || event.getEntity().isDead || event.isCanceled())
			return;
		
		if (ehWorker != null && event.getEntity() instanceof EntityItem) {
			ehWorker.recordDrop(event.getEntity());
			
			event.setCanceled(true);
			
		} else if (ehWorker != null && event.getEntity() instanceof EntityXPOrb) {
			ehWorker.addXP(((EntityXPOrb) event.getEntity()).getXpValue());
			
			event.setCanceled(true);
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
	
}
