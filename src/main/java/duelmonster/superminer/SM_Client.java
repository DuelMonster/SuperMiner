package duelmonster.superminer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class SM_Client extends SM_Proxy {

	public SM_Client() { INSTANCE = this; }
  
  public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
    EntityPlayer player = super.getPlayerFromNetHandler(handler);
    if (player == null) return getClientPlayer();
    return player;
  }
  
  public boolean isClientSideAvailable() { return true; }
  
  public EntityPlayer getClientPlayer() { return FMLClientHandler.instance().getClient().thePlayer; }
  
  public World getClientWorld() { return FMLClientHandler.instance().getClient().theWorld; }
  
  public void exectuteClientCode(IClientCode clientCode) {
    clientCode.exectuteClientCode();
  }
}
