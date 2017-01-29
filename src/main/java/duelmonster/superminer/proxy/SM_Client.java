package duelmonster.superminer.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SM_Client extends SM_Proxy {
	
	public SM_Client() {
		INSTANCE = this;
	}
	
	@Override
	public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
		EntityPlayer player = super.getPlayerFromNetHandler(handler);
		if (player == null) return getClientPlayer();
		return player;
	}
	
	@Override
	public boolean isClientSideAvailable() {
		return true;
	}
	
	@Override
	public EntityPlayer getClientPlayer() {
		return FMLClientHandler.instance().getClient().player;
	}
	
	@Override
	public World getClientWorld() {
		return FMLClientHandler.instance().getClient().world;
	}
	
	@Override
	public void exectuteClientCode(IClientCode clientCode) {
		clientCode.exectuteClientCode();
	}
}
