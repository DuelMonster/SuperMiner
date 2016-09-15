package duelmonster.superminer;

import cpw.mods.fml.common.FMLCommonHandler;
import duelmonster.superminer.events.ServerEvents;
import duelmonster.superminer.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

public class SM_Proxy {
	
	public static SM_Proxy INSTANCE;
	
	public SM_Proxy() { INSTANCE = this; }
	
	public static int multiBlockID = 0;
	
	public static Item MicroBlockId = null;
	
	public void postInit() {}
	
	public void registerEventHandler() {
		FMLCommonHandler.instance().bus().register(new ServerEvents());
	}

	public void registerRenderInformation() {}
	
	public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
		if ((handler instanceof NetHandlerPlayServer))
			return ((NetHandlerPlayServer)handler).playerEntity;
		
		return null;
	}
	
	public void throwLoadingError(String cause, String... message) {
		String concat = cause + ": ";
		for (String m : message)
			concat = concat + " - " + m;
		
		throw new RuntimeException(concat);
	}
	
	public EntityPlayer getClientPlayer() { throw new RuntimeException("getClientPlayer called on server"); }
	
	public net.minecraft.world.World getClientWorld() { throw new RuntimeException("getClientWorld called on server"); }
	
	public boolean isClientSideAvailable() { return false; }	

	public PacketHandler getNewPacketHandler() {
		return new PacketHandler();
	}
	
	public void exectuteClientCode(IClientCode clientCode) {}
}
