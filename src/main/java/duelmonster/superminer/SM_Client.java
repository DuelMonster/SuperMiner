package duelmonster.superminer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SM_Client extends SM_Proxy {

	public SM_Client() { INSTANCE = this; }
  
  public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
    EntityPlayer player = super.getPlayerFromNetHandler(handler);
    if (player == null) return getClientPlayer();
    return player;
  }
  
  public boolean isClientSideAvailable() { return true; }
  
  public EntityPlayer getClientPlayer() { return FMLClientHandler.instance().getClient().player; }
  
  public World getClientWorld() { return FMLClientHandler.instance().getClient().world; }
  
  public void exectuteClientCode(IClientCode clientCode) {
    clientCode.exectuteClientCode();
  }
}
