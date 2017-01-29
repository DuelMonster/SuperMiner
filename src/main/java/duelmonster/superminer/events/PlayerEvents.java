package duelmonster.superminer.events;

import duelmonster.superminer.SuperMiner_Core;
import duelmonster.superminer.objects.Globals;
import duelmonster.superminer.submods.Captivator;
import duelmonster.superminer.submods.Cropinator;
import duelmonster.superminer.submods.Excavator;
import duelmonster.superminer.submods.Illuminator;
import duelmonster.superminer.submods.Lumbinator;
import duelmonster.superminer.submods.Shaftanator;
import duelmonster.superminer.submods.Substitutor;
import duelmonster.superminer.submods.Veinator;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PlayerEvents {
	public static String	sLatestVersion		= "";
	private static boolean	bIsPlayerInWorld	= false;
	
	public static boolean IsPlayerInWorld() {
		return bIsPlayerInWorld;
	}
	
	@SubscribeEvent
	public void onPlayerConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		doEventStuff();
	}
	
	@SubscribeEvent
	public void onPlayerDisconnectedFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		bIsPlayerInWorld = false;
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		doEventStuff();
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedOutEvent event) {
		bIsPlayerInWorld = false;
	}
	
	private void doEventStuff() {
		if (!bIsPlayerInWorld) {
			bIsPlayerInWorld = true;
			
			CheckResult verCheck = ForgeVersion.getResult(FMLCommonHandler.instance().findContainerFor(SuperMiner_Core.MODID));
			if (verCheck.status == Status.OUTDATED)
				Globals.NotifyClient("§7" + Globals.localize("superminer.version.update") + ": v§a" + verCheck.target.toString());
			
			Captivator.bShouldSyncSettings = true;
			Cropinator.bShouldSyncSettings = true;
			Excavator.bShouldSyncSettings = true;
			Illuminator.bShouldSyncSettings = true;
			Lumbinator.bShouldSyncSettings = true;
			Shaftanator.bShouldSyncSettings = true;
			Substitutor.bShouldSyncSettings = true;
			Veinator.bShouldSyncSettings = true;
		}
	}
}
