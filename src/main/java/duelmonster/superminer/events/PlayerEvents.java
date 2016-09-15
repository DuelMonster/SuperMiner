package duelmonster.superminer.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
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

public class PlayerEvents {
	public static String sLatestVersion = "";
	private static boolean bIsPlayerInWorld = false;
	public static boolean IsPlayerInWorld() { return bIsPlayerInWorld; }

	@SubscribeEvent
	public void onPlayerConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event){
		doEventStuff();
	}

	@SubscribeEvent
	public void onPlayerDisconnectedFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
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
			
			if (sLatestVersion.isEmpty())
				sLatestVersion = Globals.getLatestVerion(SuperMiner_Core.VC_URL);
			
			if (!sLatestVersion.isEmpty() && !sLatestVersion.equalsIgnoreCase(SuperMiner_Core.VERSION))
				Globals.NotifyClient("§7" + Globals.localize("superminer.version.update") + ": v§a" + sLatestVersion);
	
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
